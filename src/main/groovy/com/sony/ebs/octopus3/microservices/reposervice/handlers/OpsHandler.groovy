package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.Operation
import com.sony.ebs.octopus3.microservices.reposervice.business.OperationEnum
import com.sony.ebs.octopus3.microservices.reposervice.business.OpsParser
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import rx.Subscriber

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe

/**
 * author: TRYavasU
 * date: 22/07/2014
 */

@Slf4j
@Component
class OpsHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {

            def params = [:]

            params.recipe = request.body.bytes
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()

            if (!params.recipe) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        OpsParser.parse(new String(params.recipe)).each { Operation operation ->
                            def parameters = operation.parameters
                            switch (operation.methodName) {
                                case OperationEnum.ZIP:
                                    repoService.zip new URNImpl(parameters.get("source"))
                                    break
                                case OperationEnum.UPLOAD:
                                    repoService.upload new URNImpl(parameters.get("source")), RepoUploadEnum.valueOf(parameters.get("destination"))
                                    break
                                case OperationEnum.COPY:
                                    repoService.copy new URNImpl(parameters.get("source")), new URNImpl(parameters.get("destination"))
                                    break
                            }
                        }
                    }
            ).subscribe(([
                    onCompleted: {
                    },
                    onNext     : { result ->
                        //unparsable json returns groovy NullObject so we need to check null object
                        if (result.equals(null)) {
                            response.status(400)
                            render json(
                                    status: 400
                            )
                        } else {
                            response.status(200)
                            render json(
                                    status: 200
                            )
                        }
                    },
                    onError    : {
                        Exception e ->
                            response.status(404)
                            render json([status: 404, message: e.message])
                    }
            ] as Subscriber))
        }
    }

}
