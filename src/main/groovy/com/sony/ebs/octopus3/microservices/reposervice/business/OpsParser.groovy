package com.sony.ebs.octopus3.microservices.reposervice.business

import groovy.json.JsonSlurper

/**
 * author: tryavasu
 * date: 11/07/2014
 */
class OpsParser {
    static Operation[] parse(String text) {
        new JsonSlurper().parseText(text).ops?.collectMany {
            it.collect { Map.Entry<String, Map<String, String>> entry ->
                new Operation(methodName: OperationEnum.valueOf(entry.key.toUpperCase()), parameters: entry.value)
            }
        }
    }
}
