package com.sony.ebs.octopus3.microservices.reposervice.business.util

import java.nio.file.Path
import java.nio.file.Paths

/**
 * author: TRYavasU
 * date: 27/06/2014
 */
class UrnUtils {

    final static def SEPARATOR = File.separator

    /**
     * Converts urn into filePath
     * @param urn (eg. urn:flix_sku:global:en_gb:xel1bu) (mandatory)
     * @return ( eg. " BASE_PATH / flix_sku / global / en_gb / xel1bu " )
     */
    static def decompose(String urn) {
        def decomposed = ""
        def parts = urn.split(":")
        for (i in 1..parts.size() - 1) {
            decomposed += parts[i] + SEPARATOR
        }
        def result = "${SEPARATOR}${decomposed}"
        SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn], "Result is ${result}")
        result
    }

    /**
     * Converts filePath to urn
     *
     * @param path ( eg. " / flix_sku / global / en_gb / xel1bu " ) ( mandatory )
     * @return urn (eg. urn:flix_sku:global:en_gb:xel1bu)
     */
    static def compose(basePath, Path path) {
        def urn = ""
        while (path != Paths.get(basePath)) {
            urn = ":${path.fileName}${urn}"
            path = path.parent
        }
        def result = "urn${urn}".toString()
        SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [basePath, path], "Result is ${result}")
        result
    }
}
