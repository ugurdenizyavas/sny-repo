package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.urn.URN
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
    def delta(URN urn, sdate, edate) {
        try {
            def result = Files.newDirectoryStream(Paths.get(basePath + urn.toPath()), [
                    accept: { Path path ->
                        def startDate = sdate ? ISODateUtils.toISODate(sdate).millis : ISODateUtils.toISODate("1970-01-01T00:00:00.000Z").millis
                        def endDate = edate ? ISODateUtils.toISODate(edate).millis : new Date().time //TODO: Change to iso date

                        def lastModified = path.toFile().lastModified()
                        lastModified > startDate && lastModified < endDate
                    }
            ] as DirectoryStream.Filter<Path>
            ).collect { path ->
                compose(basePath, path)
            }.flatten()
            result
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: Check to solve this in UrnImpl
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
        result
    }
}
