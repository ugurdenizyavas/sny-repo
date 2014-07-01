package com.sony.ebs.octopus3.microservices.reposervice.business.validation

import java.text.SimpleDateFormat

/**
 * author: TRYavasU
 * date: 26/06/2014
 */
class Validation {
    static def validateDate(String date) {
        try {
            new SimpleDateFormat("dd/MM/yyyy'T'HH:mm'Z'").parse(date)
            true
        } catch (Exception e) {
            false
        }
    }

    static def validateUrn(String urn) {
        urn.startsWith("urn:")
    }
}
