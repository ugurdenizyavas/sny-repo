package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.Operation
import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.OperationEnum
import com.sony.ebs.octopus3.microservices.reposervice.handlers.operation.OpsParser
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
@Slf4j(value = "activity")
@Component
class OpsHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {

            def params = [:]
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to ops with processId: ${params.processId}")

            params.recipe = request.body.bytes

            if (!params.recipe) {
                activity.warn "Request to ops with processId: ${params.processId} rejected."
                response.status(400)
                render json(status: 400, message: "rejected")
            } else {
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
                                activity.warn "Request to ops with processId: ${params.processId} rejected."
                                response.status(400)
                                render json(
                                        status: 400
                                )
                            } else {
                                activity.info "Request to ops with processId: ${params.processId} ok."
                                response.status(200)
                                render json(
                                        status: 200
                                )
                            }
                        },
                        onError    : {
                            Exception e ->
                                activity.warn "Request to ops with processId: ${params.processId} not found."
                                response.status(404)
                                render json([status: 404, message: e.message])
                        }
                ] as Subscriber))
            }
        }
    }

}
