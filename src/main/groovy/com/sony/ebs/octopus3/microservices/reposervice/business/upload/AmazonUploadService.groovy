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

    final def accessKeyId = "AKIAJDSMP4FH74HDUXYQ"
    final
    def secretAccessKey = "1CDE2390B90F8451B53EDA896AB918F98A2D0BE6A0B44639C70EC2175D6DAD851D49EB561FA2235609A315CB80B6FD45"
    final def bucketName = "vendorfeeds-uk"
    final def uploadPath = "sony-products/dropbox/UK"

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
                putObject(new PutObjectRequest(bucketName, uploadPath + File.separator + file.name, file))
            } catch (Exception e) {
                return false
            }

            return true
        }

    }
}
