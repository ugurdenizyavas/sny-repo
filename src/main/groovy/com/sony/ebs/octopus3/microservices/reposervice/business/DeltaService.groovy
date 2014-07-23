package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNImpl
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * author: TRYavasU
 * date: 27/06/2014
 */
@Slf4j
@Component
class DeltaService {

    @Value('${storage.root}')
    String basePath

    /**
     * Returns all urns for given criteria; filters by file's last modified date with deltaDate
     *
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param sdate Start date to check with files' last modified date. (optional, default: 01/01/1970T00:00Z)
     * @param edate End date to for the time interval. (optional, default: now)
     * @return urns of result files
     */
    def delta(URN urn, DateTime sdate, DateTime edate) {
        def startDate = sdate ? sdate.millis : 0L
        def endDate = edate ? edate.millis : DateTime.now().millis
        log.error "Delta is evaluated for urn: ${urn} startDate: ${startDate} and endDate: ${endDate}"
        if (urn != null) {
            try {
                Files.newDirectoryStream(Paths.get("${basePath}${urn.toPath()}"), [
                        accept: { Path path ->
                            def lastModified = path.toFile().lastModified()
                            def accept = lastModified >= startDate && lastModified < endDate
                            if (accept) {
                                log.debug "File ${path} with lastModifiedTime ${lastModified} is accepted"
                            } else {
                                log.debug "File ${path} with lastModifiedTime ${lastModified} is rejected"
                            }
                            accept
                        }
                ] as DirectoryStream.Filter<Path>
                ).collect { path ->
                    new URNImpl(Paths.get(basePath), path).toString()
                }.flatten()
            } catch (IOException e) {
                log.error("Delta cannot be evaluated due to errors", e)
                []
            }
        } else {
            log.error("Delta cannot be evaluated due to errors", e)
            []
        }
    }

}
