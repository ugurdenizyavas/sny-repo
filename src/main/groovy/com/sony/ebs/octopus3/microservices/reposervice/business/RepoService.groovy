package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URN
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.*
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
        Path path = Paths.get(basePath + urn.toPath())
        FileUtils.writeFile(path, file, true, true)
        if (updateDate) {
            Files.setLastModifiedTime(path, FileTime.fromMillis(ISODateUtils.toISODate(updateDate).millis))
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
    def delete(URN urn) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.exists(path)) {
            FileUtils.deleteDirectory(path)
        } else {
            null
        }
    }
}
