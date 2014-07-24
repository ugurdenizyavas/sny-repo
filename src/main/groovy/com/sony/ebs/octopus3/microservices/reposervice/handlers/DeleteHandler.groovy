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

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe

/**
 * author: TRYavasU
 * date: 22/07/2014
 */
@Slf4j(value = "activity")
@Component
class DeleteHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]

            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to delete with processId: ${params.processId.toString()}")
            try {
                params.urn = new URNImpl(pathTokens.urn)
            } catch (URNCreationException e) {
                activity.warn "Request to delete with processId: ${params.processId.toString()} rejected.", e
                response.status(400)
                render json(status: 400, processId: params.processId, response: "rejected", message: e.message)
            }

            observe(
                    blocking {
                        repoService.delete params.urn
                    }
            ) subscribe { result ->
                activity.info "Request to delete with processId: ${params.processId.toString()} OK."
                response.status(200)
                render json(status: 200, processId: params.processId, response: "OK", deletedFiles: result.tracked.collect {
                    it.toString()
                }, failedFiles: result.failed.collect { it.toString() })
            }

        }
    }

}
