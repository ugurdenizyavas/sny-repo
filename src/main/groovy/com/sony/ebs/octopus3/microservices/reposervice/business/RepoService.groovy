package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.urn.URN
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

/**
 * author: TRYavasU
 * date: 23/06/2014
 */
@Component
class RepoService {

    @Value('${storage.root}')
    String basePath

    /**
     *
     * Writes given content to file with path extracted from urn
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @param file any format is possible (mandatory)
     * @param updateDate last modification date for the new file (optional)
     * @return path of the file
     */
    void write(URN urn, file, updateDate) {
        try {
            def path = Paths.get(basePath + urn.toPath())
            Files.deleteIfExists(path)
            Files.createDirectories(path.parent)
            path << file
            if (updateDate) {
                Files.setLastModifiedTime(path, FileTime.fromMillis(ISODateUtils.toISODate(updateDate).millis))
            }
        } catch (all) {
            throw all
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
            throw new FileNotFoundException("File in path ${path} not found")
        } else {
            path
        }
    }

    /**
     * Deletes folder or file for given urn
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     */
    void delete(URN urn) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.exists(path)) {
            removeRecursive(path)
        }
    }

    static void removeRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }
        })
    }
}
