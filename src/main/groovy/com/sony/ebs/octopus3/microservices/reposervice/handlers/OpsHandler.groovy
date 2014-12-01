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
@Slf4j(value = "activity", category = "activity")
@Component
class OpsHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {

            def params = [:]
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to ops with processId: ${params.processId.toString()}")

            params.recipe = request.body.bytes

            if (!params.recipe) {
                activity.warn "Request to ops with processId: ${params.processId.toString()} rejected."
                response.status(400)
                render json(status: 400, processId: params.processId, response: "rejected", message: "request body is empty")
            } else {
                observe(
                        blocking {
                            OpsParser.parse(new String(params.recipe as byte[])).each { Operation operation ->
                                def parameters = operation.parameters
                                switch (operation.methodName) {
                                    case OperationEnum.ZIP:
                                        repoService.zip new URNImpl(parameters.get("source"))
                                        break
                                    case OperationEnum.DELETE:
                                        def pureDelete = parameters.get("pureDelete") != null ? parameters.get("pureDelete").toBoolean() : true
                                        repoService.delete new URNImpl(parameters.get("source")), pureDelete
                                        break
                                    case OperationEnum.UPLOAD:
                                        repoService.upload new URNImpl(parameters.get("source")), RepoUploadEnum.valueOf(parameters.get("destination"))
                                        break
                                    case OperationEnum.COPY:
                                        repoService.copy new URNImpl(parameters.get("source")), new URNImpl(parameters.get("destination"))
                                        break
                                    case OperationEnum.RENAME:
                                        repoService.rename new URNImpl(parameters.get("source")), parameters.get("targetName")
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
                                activity.warn "Request to ops with processId: ${params.processId.toString()} rejected."
                                response.status(400)
                                render json(status: 400, processId: params.processId, response: "rejected", message: "ops is unparsable")
                            } else {
                                activity.info "Request to ops with processId: ${params.processId.toString()} ok."
                                response.status(200)
                                render json(status: 200, processId: params.processId, response: "OK")
                            }
                        },
                        onError    : {
                            Exception e ->
                                if (e instanceof FileNotFoundException) {
                                    activity.warn "Request to ops with processId: ${params.processId.toString()} not found.", e
                                    response.status(404)
                                    render json([status: 404, processId: params.processId, response: "not found", message: e.message])
                                } else {
                                    activity.warn "Request to ops with processId: ${params.processId.toString()} server error."
                                    response.status(500)
                                    render json([status: 500, processId: params.processId, response: "server error", message: e.message])
                                }
                        }
                ] as Subscriber))
            }
        }
    }

}
