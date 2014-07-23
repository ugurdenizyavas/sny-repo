package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
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
            activity.info("Request to zip with processId: ${params.processId}")

            try {
                params.urn = new URNImpl(pathTokens.urn)

                observe(
                        blocking {
                            repoService.zip params.urn
                        }
                ).subscribe(([
                        onCompleted: {
                        },
                        onNext     : { result ->
                            activity.info "Request to zip with processId: ${params.processId} created."
                            response.status(201)
                            render json(
                                    status: 201,
                                    zippedFiles: result.collect { it.toString() },
                                    zipPath: params.urn.toPath() + ZIP_EXTENSION
                            )
                        },
                        onError    : { Exception e ->
                            activity.warn "Request to zip with processId: ${params.processId} not found.", e
                            response.status(404)
                            render json([status: 404, message: e.message])
                        }
                ] as Subscriber))
            } catch (URNCreationException e) {
                activity.warn "Request to zip with processId: ${params.processId} rejected.", e
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }
    }

}

