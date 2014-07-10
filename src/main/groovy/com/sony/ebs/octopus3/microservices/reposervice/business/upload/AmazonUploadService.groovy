package com.sony.ebs.octopus3.microservices.reposervice.business.upload

import com.amazonaws.AmazonClientException
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author ferhat sobay
 * @author tryavasu
 */
@Component
class AmazonUploadService {

    //TODO: Learn this params from Amazon
    final def accessKeyId
    final def secretAccessKey
    final def bucketName
    final def uploadPath

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
        //TODO: Parametrize accessKeyId, secretAccessKey, bucketName and uploadPath via destination
        new AmazonS3Client(
                new BasicAWSCredentials(accessKeyId, secretAccessKey),
                new ClientConfiguration().with {
                    if (proxyHostParam) setProxyHost(proxyHostParam)
                    if (proxyPortParam) setProxyPort(proxyPortParam)
                    if (proxyUsernameParam) setProxyUsername(proxyUsernameParam)
                    if (proxyUsernameParam) setProxyPassword(proxyPasswordParam)
                    if (connectionTimeoutParam) setConnectionTimeout(connectionTimeoutParam)
                }
        ).with {
            try {
                putObject(new PutObjectRequest(bucketName, uploadPath + File.separator + file.name, file))
            } catch (AmazonClientException e) {
                log.error "Unexpected error occurred while uploading amazon feed. Exception: ${e.stackTrace}"
            }
        }
    }
}
