package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.AmazonUploadService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime

/**
 * author: TRYavasU
 * date: 23/06/2014
 */
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
            Files.setLastModifiedTime(path, FileTime.fromMillis(updateDate.millis))
        }
    }

    /**
     * Returns path,which is decomposed from urn, of the file and checks if it exists
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @return path of the file
     */
    def read(URN urn) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.notExists(path)) {
            throw new FileNotFoundException("File in path ${urn.toPath()} not found")
        } else {
            path
        }
    }

    /**
     * Deletes folder or file for given urn
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     */
    def delete(URN urn) {
        FileUtils.delete(Paths.get(basePath + urn.toPath()))
    }

    /**
     * Zips directory for given urn
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param zipTarget zip file to create/overwrite
     */
    def zip(URN urn) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.notExists(path)) {
            throw new FileNotFoundException("File in path ${urn.toPath()} not found")
        } else {
            FileUtils.zip(Paths.get(path.toString() + ".zip"), path)
        }
    }

    /**
     * Copy file/folder from source to destination
     * @param sourceUrn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param destinationUrn (eg. urn:flix_sku:global:fr_fr) (mandatory)
     */
    void copy(URN sourceUrn, URN destinationUrn) {
        def sourcePath = Paths.get(basePath + sourceUrn.toPath())
        if (Files.notExists(sourcePath)) {
            throw new FileNotFoundException("File in sourcePath ${sourceUrn.toPath()} not found")
        } else {
            def destinationPath = Paths.get(basePath + destinationUrn.toPath())
            def destinationParent = destinationPath.parent
            if (Files.notExists(destinationParent)) {
                Files.createDirectories(destinationParent)
            }
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    /**
     * Copy file/folder from source to destination
     * @param sourceUrn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param destination instance of {@link RepoUploadEnum} (mandatory)
     */
    void upload(URN sourceUrn, RepoUploadEnum destination) {
        def sourcePath = Paths.get(basePath + sourceUrn.toPath())
        if (Files.notExists(sourcePath)) {
            throw new FileNotFoundException("File in sourcePath ${sourceUrn.toPath()} not found")
        } else {
            if (destination == RepoUploadEnum.S3) {
                amazonUploadService.upload(sourcePath.toFile(), destination)
            }
        }
    }
}
