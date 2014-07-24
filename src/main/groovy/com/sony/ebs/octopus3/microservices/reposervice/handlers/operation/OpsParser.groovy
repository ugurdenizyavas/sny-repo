package com.sony.ebs.octopus3.microservices.reposervice.handlers.operation

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

/**
 * author: tryavasu
 * date: 11/07/2014
 */
@Slf4j
class OpsParser {
    static Operation[] parse(String json) {
        def ops = new JsonSlurper().parseText(json).ops?.collectMany {
            it.collect { Map.Entry<String, Map<String, String>> entry ->
                new Operation(methodName: OperationEnum.valueOf(entry.key.toUpperCase()), parameters: entry.value)
            }
        }
        log.debug "Operations: ${ops.collect { it.methodName }} with params: ${ops.collect { it.parameters }} are parsed respectedly from JSON:${json}"
        ops
    }
}
