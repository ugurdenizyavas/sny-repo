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

@Slf4j
@Component
class SaveHandler extends GroovyHandler {

    @Autowired
    RepoService repoService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]

            try {
                params.file = request.body.bytes
                if (!params.file) new RuntimeException("File parameter is empty")
                params.urn = new URNImpl(pathTokens.urn)
                params.updateDate = request.queryParams.updateDate ? ISODateUtils.toISODate(request.queryParams.updateDate) : null
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            } catch (Exception e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        repoService.write params.urn, params.file, params.updateDate
                    }
            ) subscribe {
                response.status(202)
                render json(status: 202, message: "accepted")
            }
        }
    }

}
