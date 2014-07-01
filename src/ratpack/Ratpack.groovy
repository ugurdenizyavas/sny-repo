import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.business.util.SplunkLog
import com.sony.ebs.octopus3.microservices.reposervice.business.util.ProcessIdUtil
import com.sony.ebs.octopus3.microservices.reposervice.business.validation.Validation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ratpack.error.ServerErrorHandler
import ratpack.exec.Fulfiller
import ratpack.form.Form
import ratpack.handling.Context
import ratpack.jackson.Jackson
import ratpack.jackson.JacksonModule
import ratpack.rx.RxRatpack
import rx.Subscriber

import java.nio.file.Path

import static ratpack.groovy.Groovy.ratpack
import static ratpack.rx.RxRatpack.observe
import static ratpack.jackson.Jackson.json

Logger log = LoggerFactory.getLogger("ratpack");

ratpack {

    RepoService repoService
    DeltaService deltaService

    def SERVICE_NAME = "RepoService"

    bindings {
        add new JacksonModule()

        bind ServerErrorHandler, new ServerErrorHandler() {
            @Override
            void error(Context context, Exception exception) {
                log.error "error", exception
            }
        }
        init {
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
            SpringConfig.setLaunchConfig(ctx, launchConfig)

            repoService = ctx.getBean(RepoService.class)
            deltaService = ctx.getBean(DeltaService.class)

            RxRatpack.initialize()
            Jackson
        }
    }

    handlers {
        get("repository") {
            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.STARTED, null)
            def result = "Welcome to Repo Service"
            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.DONE, result)
            render result
        }

        //Repo Service
        post("repository/file/write/:urn") {
            Form form = parse(Form)
            def urn = pathTokens['urn']
            def file = form.file("file").getBytes()
            def processId = context.request.queryParams['processId']
            processId = ProcessIdUtil.generateId(processId)

            SplunkLog.logRatpack(processId, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.STARTED, null)

            if (Validation.validateUrn(urn) && file) {
                observe(
                        blocking {
                            SplunkLog.logRatpack(processId, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.PROCESSING, null)
                            repoService.write urn, file
                        }
                ) subscribe {
                    SplunkLog.logRatpack(processId, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.DONE, null)
                    render "accepted"
                }
            } else {
                SplunkLog.logRatpack(processId, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.REJECTED, null)
                context.response.status(400)
                render "rejected"
            }
        }

        get("repository/file/read/:urn") {
            def urn = pathTokens['urn']
            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.STARTED, null)

            if (Validation.validateUrn(urn)) {
                observe(
                        blocking {
                            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.PROCESSING, null)
                            repoService.read(urn)
                        }
                ).subscribe(([
                        onCompleted: {
                        },
                        onNext     : { Path result ->
                            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.DONE, result)
                            response.sendFile(context, result)
                        },
                        onError    : { Exception e ->
                            context.response.status(404)
                            render json([code: 404, message: e.message])
                        }
                ] as Subscriber<Path>))
            } else {
                SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.REJECTED, null)
                context.response.status(400)
                render "rejected"
            }
        }

        //Delta Service
        get("repository/delta/:urn") {
            def urn = pathTokens['urn']
            def deltaDate = context.request.queryParams['deltaDate']

            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.STARTED, null)

            if (Validation.validateUrn(urn) && (!deltaDate || Validation.validateDate(deltaDate))) {
                observe(
                        promise { Fulfiller fulfiller ->
                            SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.PROCESSING, null)
                            Thread.start {
                                fulfiller.success(
                                        deltaService.delta(urn, deltaDate)
                                )
                            }
                        }
                ) subscribe { result ->
                    SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.DONE, result)
                    render json(result)
                }

            } else {
                SplunkLog.logRatpack(null, SERVICE_NAME, context.request.uri, SplunkLog.ProcessStatus.REJECTED, result)
                context.response.status(400)
                render "rejected"
            }
        }
    }

}
