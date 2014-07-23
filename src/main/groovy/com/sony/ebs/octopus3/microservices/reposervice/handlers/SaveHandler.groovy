package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.date.ISODateUtils
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

import java.nio.file.Path

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe

/**
 * author: TRYavasU
 * date: 22/07/2014
 */
@Slf4j(value = "activity")
@Component
class SaveHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to save with processId: ${params.processId}")

            params.file = request.body.bytes
            if (!params.file) {
                activity.warn "Request to save with processId: ${params.processId} rejected."
                response.status(400)
                render json(status: 400, response: "rejected", message: "request body is empty")
            } else {
                try {
                    params.urn = new URNImpl(pathTokens.urn)
                    params.updateDate = request.queryParams.updateDate ? ISODateUtils.toISODate(request.queryParams.updateDate) : null

                    observe(
                            blocking {
                                repoService.write params.urn, params.file, params.updateDate
                            }
                    ).subscribe(([
                            onCompleted: {
                            },
                            onNext     : { Path result ->
                                activity.info "Request to save with processId: ${params.processId} accepted."
                                response.status(200)
                                render json(status: 200, processId: params.processId, response: "OK")
                            },
                            onError    : { Exception e ->
                                activity.warn "Request to read with processId: ${params.processId} server error."
                                response.status(500)
                                render json([status: 500, processId: params.processId, response: "server error", message: e.message])
                            }
                    ] as Subscriber<Path>))
                } catch (URNCreationException e) {
                    activity.warn "Request to save with processId: ${params.processId} rejected."
                    response.status(400)
                    render json(status: 400, processId: params.processId, response: "rejected", message: e.message)
                }
            }

        }
    }

}
