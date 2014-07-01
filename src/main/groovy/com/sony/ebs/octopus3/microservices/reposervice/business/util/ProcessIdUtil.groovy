package com.sony.ebs.octopus3.microservices.reposervice.business.util

/**
 * author: TRYavasU
 * date: 25/06/2014
 */
class ProcessIdUtil {

    /**
     * Generates a UUID if process has no processId
     *
     * @param processId (UUID of the process) (optional)
     * @return unique processId
     */
    static def generateId(String processId) {
        processId ?: UUID.randomUUID().toString()
    }
}
