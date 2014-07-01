package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.microservices.reposervice.business.util.SplunkLog
import com.sony.ebs.octopus3.microservices.reposervice.business.util.UrnUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ratpack.launch.LaunchConfig

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

/**
 * author: TRYavasU
 * date: 27/06/2014
 */
@Component
class DeltaService {

    @Value('${octopus3.reposervice.storageFolder}')
    def basePath

    final static def DATE_FORMAT = "dd/MM/yyyy'T'HH:mm'Z'"

    LaunchConfig launchConfig

    /**
     * Returns all urns for given criteria; filters by file's last modified date with deltaDate
     *
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param deltaDate (optional)
     * @return contents of files
     */
    def delta(urn, deltaDate) {
        SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn, deltaDate], "Starting Delta")
        try {
            def result = Files.newDirectoryStream(Paths.get(basePath + UrnUtils.decompose(urn)), new DirectoryStream.Filter<Path>() {
                @Override
                boolean accept(Path path) throws IOException {
                    //if deltaDate is null, accept all the files;
                    //else take the ones whose last modified date is after then deltaDate
                    !deltaDate || (deltaDate && path.toFile().lastModified() > new SimpleDateFormat(DATE_FORMAT).parse(deltaDate).time)
                }
            }).collect { path ->
                UrnUtils.compose(basePath, path)
            }.flatten()
            SplunkLog.logService(this.class.name, Thread.currentThread().stackTrace[10].methodName, [urn], "Delta returns result: ${result}")
            result
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
