package com.sony.ebs.octopus3.microservices.reposervice.business.upload

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author ferhat sobay
 * @author tryavasu
 */
@Slf4j
@Component
class AmazonUploadService {

    final def accessKeyId = "AKIAJ5L6DQY2UZBBVMYQ"
    final
    def secretAccessKey = "6BD39073F27D4DF86B796632DCB7F9C2833A183FE8E297CA8A35C239E8744402FCC999457B1AB4917CDB44E266398B11"
    final def bucketName = "sony-products"
    final def uploadPath = "dropbox/Sony\\u0020Test\\u0020Folder"

    @Value('${amazon.s3.proxyHost}')
    String proxyHostParam
    @Value('${amazon.s3.proxyPort}')
    Integer proxyPortParam
    @Value('${amazon.s3.proxyUsername}')
    String proxyUsernameParam
    @Value('${amazon.s3.proxyPassword}')
    String proxyPasswordParam
    @Value('${amazon.s3.connectionTimeout}')
    Integer connectionTimeoutParam

    void upload(file, destination) {

        log.debug "Amazon feed upload with proxyHost: ${proxyHostParam}, proxyPort: ${proxyPortParam}, proxyUsername: ${proxyUsernameParam}, proxyPassword: ${proxyPasswordParam} and connectionTimeout:${connectionTimeoutParam}"

        //TODO: Add multi destination feature
        new AmazonS3Client(new BasicAWSCredentials(accessKeyId, secretAccessKey),
                new ClientConfiguration(
                        proxyHost: proxyHostParam,
                        proxyPort: proxyPortParam,
                        proxyUsername: proxyUsernameParam,
                        proxyPassword: proxyPasswordParam,
                        connectionTimeout: connectionTimeoutParam
                )
        ).with {
            try {
                log.info "Sending feed to amazon with bucketName:${bucketName} and uploadPath:${uploadPath}"
                putObject(new PutObjectRequest(bucketName, uploadPath + File.separator + file.name, file))
            } catch (Exception e) {
                log.error "Cannot send feed to amazon", e
            }
        }

    }
}
