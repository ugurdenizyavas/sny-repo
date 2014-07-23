package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
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
class SaveHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to save with processId: ${params.processId}")

            try {
                params.file = request.body.bytes
                if (!params.file) new RuntimeException("File parameter is empty")
                params.urn = new URNImpl(pathTokens.urn)
                params.updateDate = request.queryParams.updateDate ? ISODateUtils.toISODate(request.queryParams.updateDate) : null

                observe(
                        blocking {
                            repoService.write params.urn, params.file, params.updateDate
                        }
                ) subscribe {
                    activity.info "Request to save with processId: ${params.processId} accepted."
                    response.status(202)
                    render json(status: 202, message: "accepted")
                }
            } catch (Exception e) {
                activity.warn "Request to save with processId: ${params.processId} rejected."
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }
    }

}
