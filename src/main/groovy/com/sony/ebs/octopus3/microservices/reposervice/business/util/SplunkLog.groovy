package com.sony.ebs.octopus3.microservices.reposervice.business.util

import groovy.util.logging.Slf4j

/**
 * author= TRYavasU
 * date= 25/06/2014
 */
@Slf4j
class SplunkLog {

    final static String SERVICE_NAME
    //Splunk Seperator
    final static String SS = ","

    /**
     * ONLY logs ratpack services according to Splunk Logging Format
     *
     * @param processId unique processId for process (optional)
     * @param service name of the service (mandatory)
     * @param uri request url of the service (mandatory)
     * @param status current status of the process (mandatory)
     * @param result result of the service call (optional)
     */
    static void logRatpack(processId, service, uri, status, result) {
        log.info "${SS} ProcessId= ${processId}${SS} Service= ${service}${SS} URI= ${uri}${SS} Status= ${status}${SS} Result= ${result}${SS} Time= \"${new Date().toString()}\""
    }

    static def logService(serviceName, method, parameters, message) {
        log.debug "${SS} Class= ${serviceName}${SS} Method= ${method}${SS} Parameters= ${parameters}${SS} Message = \"${message}\"${SS} Time= \"${new Date().toString()}\""
    }

    enum ProcessStatus {
        STARTED, PROCESSING, PENDING, REJECTED, DONE
    }
}
