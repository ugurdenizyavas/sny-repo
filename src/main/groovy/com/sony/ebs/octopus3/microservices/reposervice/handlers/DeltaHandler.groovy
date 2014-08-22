package com.sony.ebs.octopus3.microservices.reposervice.handlers

import com.sony.ebs.octopus3.commons.date.DateConversionException
import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URN
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.exec.Fulfiller
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe

/**
 * author: TRYavasU
 * date: 22/07/2014
 */
@Slf4j(value = "activity", category = "activity")
@Component
class DeltaHandler extends GroovyHandler {

    @Autowired
    DeltaService deltaService

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def params = [:]
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            activity.info("Request to delta with processId: ${params.processId.toString()}")

            try {
                params.urn = new URNImpl(pathTokens.urn)
                params.sdate = request.queryParams.sdate ? ISODateUtils.toISODate(request.queryParams.sdate) : null
                params.edate = request.queryParams.edate ? ISODateUtils.toISODate(request.queryParams.edate) : null
            } catch (URNCreationException | DateConversionException e) {
                activity.warn "Request to delta with processId: ${params.processId.toString()} rejected."
                response.status(400)
                render json(status: 400, processId: params.processId, response: "rejected", message: e.message)
                return
            }

            observe(
                    promise { Fulfiller fulfiller ->
                        Thread.start {
                            fulfiller.success(
                                    deltaService.delta(params.urn as URN, params.sdate as DateTime, params.edate as DateTime)
                            )
                        }
                    }
            ) subscribe { result ->
                activity.info "Request to delta with processId: ${params.processId.toString()} OK."
                response.status(200)
                render json(status: 200, processId: params.processId, response: "OK", results: result)
            }
        }
    }

}
