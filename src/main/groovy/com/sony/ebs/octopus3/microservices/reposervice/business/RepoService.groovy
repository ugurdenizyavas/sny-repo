package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.urn.URN
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ratpack.launch.LaunchConfig

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * author: TRYavasU
 * date: 23/06/2014
 */
@Component
class RepoService {

    @Value('${storage.root}')
    String basePath

    LaunchConfig launchConfig

    /**
     *
     * Writes given content to file with path extracted from urn
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @param content any format is possible (mandatory)
     * @return path of the file
     */
    Path write(URN urn, file) {
        try {
            def path = Paths.get(basePath + urn.toPath())
            Files.deleteIfExists(path)
            Files.createDirectories(path.parent)
            def result = path << file
            result
        } catch (all) {
            throw all
        } finally {

        }
    }

    /**
     * Returns path,which is decomposed from urn, of the file and checks if it exists
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @return content of the file
     */
    def read(URN urn) {
        def path = Paths.get(basePath + urn.toPath())
        if (Files.notExists(path)) {
            throw new FileNotFoundException("File in path ${path} not found")
        } else {
            new File(path.toFile().absolutePath).text
        }
    }

}
