import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
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
import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe

Logger log = LoggerFactory.getLogger("ratpack");

ratpack {

    RepoService repoService
    DeltaService deltaService

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
            def result = "Welcome to Repo Service"
            render result
        }

        //Repo Service
        post("repository/file/write/:urn") {
            Form form = parse(Form)
            def urnStr = pathTokens.urn
            def file = form.file("file").getBytes()
            def processIdStr = context.request.queryParams.processId

            def urn = new URNImpl(urnStr)
            def processId = new ProcessIdImpl(processIdStr)

            if (file) {
                observe(
                        blocking {
                            repoService.write urn, file
                        }
                ) subscribe {
                    render "accepted"
                }
            } else {
                context.response.status(400)
                render "rejected"
            }
        }

        get("repository/file/read/:urn") {
            def urn = pathTokens.urn

            observe(
                    blocking {
                        repoService.read(urn)
                    }
            ).subscribe(([
                    onCompleted: {
                    },
                    onNext : { Path result ->
                        response.sendFile(context, result)
                    },
                    onError : { Exception e ->
                        context.response.status(404)
                        render json([code: 404, message: e.message])
                    }
            ] as Subscriber<Path>))
        }

        //Delta Service
        get("repository/delta/:urn") {
            def urn = pathTokens['urn']
            def deltaDate = context.request.queryParams['deltaDate']

            if (Validation.validateUrn(urn) && (!deltaDate || Validation.validateDate(deltaDate))) {
                observe(
                        promise { Fulfiller fulfiller ->
                            Thread.start {
                                fulfiller.success(
                                        deltaService.delta(urn, deltaDate)
                                )
                            }
                        }
                ) subscribe { result ->
                    render json(result)
                }

            } else {
                context.response.status(400)
                render "rejected"
            }
        }
    }

}
