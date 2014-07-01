package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.microservices.reposervice.business.util.SplunkLog
import com.sony.ebs.octopus3.microservices.reposervice.business.util.UrnUtils
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

    @Value('${octopus3.reposervice.storageFolder}')
    def basePath

    LaunchConfig launchConfig

    /**
     *
     * Writes given content to file with path extracted from urn
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @param content any format is possible (mandatory)
     * @return path of the file
     */
    Path write(urn, file) {
        SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn, file], "Starting to write")
        try {
            def path = Paths.get(basePath + UrnUtils.decompose(urn).toString())
            Files.deleteIfExists(path)
            Files.createDirectories(path.parent)
            def result = path << file
            SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn, file], "Result is : ${result}")
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
    def read(urn) {
        SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn], "Starting to read")
        def path = Paths.get(basePath + UrnUtils.decompose(urn))
        if (Files.notExists(path)) {
            SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn], "File not found in path : ${path}")
            throw new FileNotFoundException("File in path ${path} not found")
        } else {
            SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn], "Result is : ${path}")
            path
        }
    }

}
