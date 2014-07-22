package com.sony.ebs.octopus3.microservices.reposervice.business.upload

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author ferhat sobay
 * @author tryavasu
 */
@Component
class AmazonUploadService {

    private static final Logger logger = LoggerFactory.getLogger(AmazonUploadService.class)

    final def accessKeyId = "AKIAJDSMP4FH74HDUXYQ"
    final
    def secretAccessKey = "1CDE2390B90F8451B53EDA896AB918F98A2D0BE6A0B44639C70EC2175D6DAD851D49EB561FA2235609A315CB80B6FD45"
    final def bucketName = "vendorfeeds-uk"
    final def uploadPath = "sony-products/dropbox/UK"

    //TODO: Clarify these params with TCS
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
        def clientConfiguration = new ClientConfiguration()

        clientConfiguration.with {
            if (proxyHostParam) setProxyHost(proxyHostParam)
            if (proxyPortParam) setProxyPort(proxyPortParam)
            if (proxyUsernameParam) setProxyUsername(proxyUsernameParam)
            if (proxyUsernameParam) setProxyPassword(proxyPasswordParam)
            if (connectionTimeoutParam) setConnectionTimeout(connectionTimeoutParam)
        }

        def myCredentials = new BasicAWSCredentials(
                accessKeyId, secretAccessKey)

        def s3client = new AmazonS3Client(myCredentials, clientConfiguration)

        s3client.with {
            try {
                putObject(new PutObjectRequest(bucketName, uploadPath + File.separator + file.name, file))
            } catch (Exception e) {
                logger.error("Unexpected error occurred while uploading amazon images. Exception", e)
                return false
            }

            return true
        }
    }
}
