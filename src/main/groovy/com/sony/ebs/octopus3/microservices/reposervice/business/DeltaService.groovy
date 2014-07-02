package com.sony.ebs.octopus3.microservices.reposervice.business

import com.sony.ebs.octopus3.commons.urn.URN
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

    @Value('${storage.root}')
    String basePath

    LaunchConfig launchConfig

    /**
     * Returns all urns for given criteria; filters by file's last modified date with deltaDate
     *
     * @param urn (eg. urn:flix_sku:global:en_gb) (mandatory)
     * @param deltaDate (optional)
     * @return contents of files
     */
    def delta(URN urn, deltaDate) {
        try {
            def result = Files.newDirectoryStream(Paths.get(new URI(basePath + urn.toPath())), new DirectoryStream.Filter<Path>() {
                @Override
                boolean accept(Path path) throws IOException {
                    //if deltaDate is null, accept all the files;
                    //else take the ones whose last modified date is after then deltaDate
                    !deltaDate || (deltaDate && path.toFile().lastModified() > new SimpleDateFormat(DATE_FORMAT).parse(deltaDate).time)
                }
            }).collect { path ->
                UrnUtils.compose(basePath, path)
            }.flatten()
            result
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
