import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ratpack.error.ServerErrorHandler
import ratpack.exec.Fulfiller
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

            repoService = ctx.getBean(RepoService.class)
            deltaService = ctx.getBean(DeltaService.class)

            RxRatpack.initialize()
            Jackson
        }
    }

    handlers {
        get("repository") {
            render json(status: 200, message: "Welcome to Repo Service")
        }

        //Repo Service
        prefix("repository/file/:urn") {
            handler {
                byMethod {
                    post() {
                        def file = request.body.bytes
                        def urnStr = pathTokens.urn
                        def updateDate = request.queryParams.updateDate
                        def processIdStr = request.queryParams.processId

                        try {
                            def urn = new URNImpl(urnStr)
                            def processId = new ProcessIdImpl(processIdStr)

                            if (file) {
                                observe(
                                        blocking {
                                            repoService.write urn, file, updateDate
                                        }
                                ) subscribe {
                                    response.status(202)
                                    render json(status: 202, message: "accepted")
                                }
                            } else {
                                response.status(400)
                                render json(status: 400, message: "rejected")
                            }
                        } catch (URNCreationException e) {
                            response.status(400)
                            render json(status: 400, message: "rejected")
                        }

                    }

                    get() {
                        def urnStr = pathTokens.urn

                        try {
                            def urn = new URNImpl(urnStr)

                            observe(
                                    blocking {
                                        repoService.read(urn)
                                    }
                            ).subscribe(([
                                    onCompleted: {
                                    },
                                    onNext     : { Path result ->
                                        response.sendFile context, result
                                    },
                                    onError    : { Exception e ->
                                        response.status(404)
                                        render json([code: 404, message: e.message])
                                    }
                            ] as Subscriber<Path>))
                        } catch (URNCreationException e) {
                            response.status(400)
                            render json(status: 400, message: "rejected")
                        }
                    }

                    delete() {
                        def urnStr = pathTokens.urn

                        try {
                            def urn = new URNImpl(urnStr)

                            observe(
                                    blocking {
                                        repoService.delete urn
                                    }
                            ) subscribe { result ->
                                response.status(202)
                                render json(status: 202, deletedFiles: result.collect { it.toString() })
                            }
                        } catch (URNCreationException e) {
                            response.status(400)
                            render json(status: 400, message: "rejected")
                        }
                    }
                }
            }
        }

        //Delta Service
        get("repository/delta/:urn") {
            def urnStr = pathTokens.urn
            def sdate = request.queryParams.sdate
            def edate = request.queryParams.edate

            try {
                def urn = new URNImpl(urnStr)

                observe(
                        promise { Fulfiller fulfiller ->
                            Thread.start {
                                fulfiller.success(
                                        deltaService.delta(urn, sdate, edate)
                                )
                            }
                        }
                ) subscribe { result ->
                    render json(result)
                }
            } catch (URNCreationException e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }
    }

}
