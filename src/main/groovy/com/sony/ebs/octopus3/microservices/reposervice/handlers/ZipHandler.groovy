package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
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
class ZipHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            final def ZIP_EXTENSION = ".zip"
            def params = [:]

            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to zip with processId: ${params.processId.toString}")
            try {
                params.urn = new URNImpl(pathTokens.urn)
            } catch (URNCreationException e) {
                activity.warn "Request to zip with processId: ${params.processId.toString} rejected.", e
                response.status(400)
                render json(status: 400, processId: params.processId, response: "rejected", message: e.message)
                return
            }

            observe(
                    blocking {
                        repoService.zip params.urn as URN
                    }
            ).subscribe(([
                    onCompleted: {
                    },
                    onNext     : { result ->
                        activity.info "Request to zip with processId: ${params.processId.toString} created."
                        response.status(201)
                        render json(
                                status: 201, processId: params.processId, response: "created",
                                zippedFiles: result.collect { it.toString() },
                                zipPath: params.urn.toPath() + ZIP_EXTENSION
                        )
                    },
                    onError    : {
                        Exception e ->
                            if (e instanceof FileNotFoundException) {
                                activity.warn "Request to zip with processId: ${params.processId.toString} not found.", e
                                response.status(404)
                                render json([status: 404, processId: params.processId, response: "not found", message: e.message])
                            } else {
                                activity.warn "Request to zip with processId: ${params.processId.toString} server error."
                                response.status(500)
                                render json([status: 500, processId: params.processId, response: "server error", message: e.message])
                            }
                    }
            ] as Subscriber))
        }
    }

}

