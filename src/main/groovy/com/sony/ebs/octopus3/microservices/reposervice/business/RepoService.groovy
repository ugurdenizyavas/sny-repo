package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.AmazonUploadService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.security.InvalidParameterException

/**
 * author: TRYavasU
 * date: 23/06/2014
 */
@Slf4j
@Component
class RepoService {

    @Value('${storage.root}')
    String basePath

    @Autowired
    AmazonUploadService amazonUploadService

    /**
     *
     * Writes given content to file with path extracted from urn
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @param file any format is possible (mandatory)
     * @param updateDate last modification date for the new file (optional)
     * @return path of the file
     */
    void write(URN urn, byte[] file, DateTime updateDate) {
        Path path = Paths.get(basePath + urn.toPath())
        FileUtils.writeFile(path, file, true, true)
        if (updateDate) {
            def lastModifiedTime = FileTime.fromMillis(updateDate.millis)
            Files.setLastModifiedTime(path, lastModifiedTime)
            log.debug("File in path ${path} has set its last modification time to ${lastModifiedTime}");
        }
    }

    /**
     * Returns path,which is decomposed from urn, of the file and checks if it exists
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @return path of the file
     */
    def read(URN urn, readFolder = false) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.notExists(path)) {
            log.debug("File in path ${path} is not found");
            throw new FileNotFoundException("File in path ${urn.toPath()} not found")
        } else if (!readFolder && Files.readAttributes(path, BasicFileAttributes.class)?.directory) {
            log.debug("File in path ${path} is a directory");
            throw new UnsupportedOperationException("File in path ${urn.toPath()} is not a file, it is a folder")
        } else {
            path
        }
    }

    /**
     * Deletes folder or file for given urn
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     */
    def delete(URN urn, boolean failIfNoFile = true) {
        FileUtils.delete(Paths.get(basePath + urn.toPath()), failIfNoFile)
    }

    /**
     * Zips directory for given urn
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param zipTarget zip file to create/overwrite
     */
    def zip(URN urn) {
        def path = read(urn, true)
        FileUtils.zip(Paths.get(path.toString() + ".zip"), path)
    }

    /**
     * Copy file/folder from source to destination
     * @param sourceUrn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param destinationUrn (eg. urn:flix_sku:global:fr_fr) (mandatory)
     */
    void copy(URN sourceUrn, URN destinationUrn) {
        def sourcePath = read(sourceUrn, true)
        def destinationPath = Paths.get(basePath + destinationUrn.toPath())
        def destinationParent = destinationPath.parent
        if (Files.notExists(destinationParent)) {
            log.debug("Destination path ${sourcePath} does not exist, so creating parent folder")
            Files.createDirectories(destinationParent)
        }
        FileUtils.copy(sourcePath, destinationPath)
        log.debug("File in path ${sourcePath} is copied to ${destinationPath}")
    }

    /**
     * Upload file/folder from source to destination
     * @param sourceUrn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param destination instance of {@link RepoUploadEnum} (mandatory)
     */
    void upload(URN sourceUrn, RepoUploadEnum destination) {
        if (destination == RepoUploadEnum.S3) {
            amazonUploadService.upload(read(sourceUrn).toFile(), destination)
        } else {
            log.debug("Upload function to ${destination} does not exist");
            throw new RuntimeException("Upload function to ${destination} does not exist")
        }
    }

    /**
     * Returns fileAttributes,which is decomposed from urn, of the file and checks if it exists
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @return FileAttributes of the file
     */
    def getFileAttributes(URN urn) {
        def path = read(urn, true)

        createFileAttributes(urn, Files.readAttributes(path, BasicFileAttributes.class), path, true)
    }

    def createFileAttributes(URN urn, BasicFileAttributes basicFileAttributes, Path path, boolean inDepth) {
        def map = [
                urn             : urn.toString(),
                lastModifiedTime: getDateAsIsoString(basicFileAttributes?.lastModifiedTime()),
                creationTime    : getDateAsIsoString(basicFileAttributes?.creationTime()),
                lastAccessTime  : getDateAsIsoString(basicFileAttributes?.lastAccessTime()),
                directory       : basicFileAttributes?.directory,
                regularFile     : basicFileAttributes?.regularFile
        ]
        if (inDepth) {
            map << [contentFiles: (basicFileAttributes?.directory) ? fileList(path) : null]
        }
        if (!basicFileAttributes?.directory) {
            map << [size: basicFileAttributes?.size()]
        }
        map
    }

    def fileList(path) {
        Files.newDirectoryStream(path).collect { content ->
            def basicFileAttributes = Files.readAttributes(content, BasicFileAttributes.class)
            createFileAttributes(new URNImpl(Paths.get(basePath), content), basicFileAttributes, content, false)
        }
    }

    def getDateAsIsoString = { FileTime fileTime ->
        ISODateUtils.toISODateString(new DateTime(fileTime?.toMillis()))
    }

    def rename(URN urn, String targetName) throws InvalidParameterException {
        validateFileName(targetName)
        def fileToRename = read(urn)
        try {
            Files.move(fileToRename, Paths.get("${fileToRename.parent}/${targetName}"))
        } catch (IOException e) {
            log.debug("Problem with renaming file ${urn.toString()} to target ${targetName}", e)
        }
    }

    static def validateFileName(String fileName) throws InvalidParameterException {
        if (fileName.contains("\\") || fileName.contains("/") || fileName.contains("*") || fileName.contains("?") || fileName.contains(":") || fileName.contains("\"") || fileName.contains("<") || fileName.contains(">") || fileName.contains("|") || fileName.contains(" ")) {
            log.debug("File cannot be renamed to fileName: ${fileName} because fileName contains an invalid character")
            throw new InvalidParameterException("Filename: ${fileName} is not valid")
        }
    }
}
