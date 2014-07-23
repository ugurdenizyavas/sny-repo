package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
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
class ReadHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]

            try {
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
                activity.info("Request to read with processId: ${params.processId}")
                params.urn = new URNImpl(pathTokens.urn)

                observe(
                        blocking {
                            repoService.read(params.urn)
                        }
                ).subscribe(([
                        onCompleted: {
                        },
                        onNext     : { Path result ->
                            activity.info "Request to read with processId: ${params.processId} OK."
                            response.sendFile context, result
                        },
                        onError    : { Exception e ->
                            activity.warn "Request to read with processId: ${params.processId} not found."
                            response.status(404)
                            render json([status: 404, message: e.message])
                        }
                ] as Subscriber<Path>))
            } catch (Exception e) {
                activity.warn "Request to read with processId: ${params.processId} rejected."
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }
    }

}
