package com.sony.ebs.octopus3.microservices.reposervice.business

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * author: tryavasu
 * date: 11/07/2014
 */
@ToString
@EqualsAndHashCode
class Operation {
    OperationEnum methodName
    Map<String, String> parameters
}

enum OperationEnum {
    ZIP, COPY, UPLOAD, DELETE
}
