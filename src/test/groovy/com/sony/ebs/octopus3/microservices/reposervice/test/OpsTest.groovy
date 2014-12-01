package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.Operation
import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.OperationEnum
import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.OpsParser
import org.junit.Test
import org.springframework.core.io.ClassPathResource

/**
 * author: TRYavasU
 * date: 10/07/2014
 */
class OpsTest {

    @Test
    void testOperations() {
        def result = OpsParser.parse(new ClassPathResource("ops/ops.json").file.text)

        assert result.size(), 4
        assert result[0], new Operation(methodName: OperationEnum.ZIP, parameters: [source: "flix_sku:global:en_gb"])
        assert result[1], new Operation(methodName: OperationEnum.COPY, parameters: [source: "flix_sku:global:en_gb", destination: "urn:archive:flix_sku:global:en_gb.zip"])
        assert result[2], new Operation(methodName: OperationEnum.UPLOAD, parameters: [source: "flix_sku:global:en_gb", destination: "S3"])
        assert result[3], new Operation(methodName: OperationEnum.DELETE, parameters: [source: "flix_sku:global:en_gb", pureDelete: "true"])
    }

    @Test
    void testOperations2() {
        def result = OpsParser.parse(new ClassPathResource("ops/ops2.json").file.text)

        assert result.size(), 3
        assert result[0], new Operation(methodName: OperationEnum.ZIP, parameters: [source: "flix_sku:global:en_gb"])
        assert result[1], new Operation(methodName: OperationEnum.COPY, parameters: [source: "flix_sku:global:en_gb", destination: "urn:archive:flix_sku:global:en_gb.zip"])
        assert result[2], new Operation(methodName: OperationEnum.DELETE, parameters: [source: "flix_sku:global:en_gb", pureDelete: "false"])
    }
}
