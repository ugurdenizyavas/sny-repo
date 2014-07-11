import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.AnnotationConfigUtils
import org.springframework.context.support.GenericGroovyApplicationContext
import ratpack.error.DebugErrorHandler
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
        bind ServerErrorHandler, new DebugErrorHandler()
        init {
            cx = new GenericGroovyApplicationContext()
            cx.load("config.groovy");
            AnnotationConfigUtils.registerAnnotationConfigProcessors(cx);
            cx.beanFactory.registerSingleton "launchConfig", launchConfig
            cx.beanFactory.registerSingleton "execControl", launchConfig.execController.control
            cx.refresh();
            RxRatpack.initialize()
        }
    }

    handlers {
        get("repository") {
            render json(status: 200, message: "Welcome to Repo Service")
        }

        //Repo Service
        handler("repository/file/:urn") {
            byMethod {

                post() {
                    def params = [:]

                    try {
                        params.file = request.body.bytes
                        if (!params.file) new RuntimeException("File parameter is empty")
                        params.urn = new URNImpl(pathTokens.urn)
                        params.updateDate = request.queryParams.updateDate ? ISODateUtils.toISODate(request.queryParams.updateDate) : null
                        params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : null
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

                get() {
                    def params = [:]

                    try {
                        params.urn = new URNImpl(pathTokens.urn)
                    } catch (Exception e) {
                        response.status(400)
                        render json(status: 400, message: "rejected")
                    }

                    observe(
                            blocking {
                                repoService.read(params.urn)
                            }
                    ).subscribe(([
                            onCompleted: {
                            },
                            onNext     : { Path result ->
                                response.sendFile context, result
                            },
                            onError    : { Exception e ->
                                response.status(404)
                                render json([status: 404, message: e.message])
                            }
                    ] as Subscriber<Path>))
                }

                delete() {
                    def params = [:]

                    try {
                        params.urn = new URNImpl(pathTokens.urn)
                    } catch (Exception e) {
                        response.status(400)
                        render json(status: 400, message: "rejected")
                    }

                    observe(
                            blocking {
                                repoService.delete params.urn
                            }
                    ) subscribe { result ->
                        response.status(202)
                        render json(status: 202, deletedFiles: result.collect { it.toString() })
                    }

                }
            }
        }

        //OPS
        get("repository/zip/:urn") {
            final def ZIP_EXTENSION = ".zip"
            def params = [:]
            try {
                params.urn = new URNImpl(pathTokens.urn)
            } catch (URNCreationException e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        repoService.zip params.urn
                    }
            ).subscribe(([
                    onCompleted: {
                    },
                    onNext     : { result ->
                        response.status(202)
                        render json(
                                status: 202,
                                zippedFiles: result.collect { it.toString() },
                                zipPath: params.urn.toPath() + ZIP_EXTENSION
                        )
                    },
                    onError    : { Exception e ->
                        response.status(404)
                        render json([status: 404, message: e.message])
                    }
            ] as Subscriber))

        }

        get("repository/file/copy/source/:source/destination/:destination") {
            def sourceStr = pathTokens.source
            def destinationStr = pathTokens.destination
            try {
                def sourceUrn = new URNImpl(sourceStr)
                def destinationUrn = new URNImpl(destinationStr)
                observe(
                        blocking {
                            repoService.copy sourceUrn, destinationUrn
                        }
                ).subscribe(([
                        onCompleted: {
                        },
                        onNext     : {
                            response.status(202)
                            render json(status: 202, message: "accepted")
                        },
                        onError    : { Exception e ->
                            response.status(404)
                            render json([status: 404, message: e.message])
                        }
                ] as Subscriber))
            } catch (URNCreationException e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }

        get("repository/file/upload/source/:source/destination/:destination") {
            def sourceStr = pathTokens.source
            def destinationStr = pathTokens.destination
            try {
                def sourceUrn = new URNImpl(sourceStr)
                try {
                    def destination = RepoUploadEnum.valueOf(destinationStr)
                    observe(
                            blocking {
                                repoService.upload sourceUrn, destination
                            }

                    ).subscribe(([
                            onCompleted: {
                            },
                            onNext     : {
                                response.status(202)
                                render json(status: 202, message: "accepted")
                            },
                            onError    : { Exception e ->
                                response.status(404)
                                render json([status: 404, message: e.message])
                            }
                    ] as Subscriber))
                } catch (IllegalArgumentException e) {
                    response.status(400)
                    render json(status: 400, message: "rejected")
                }
            } catch (URNCreationException e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }
        }

        //Delta Service
        get("repository/delta/:urn") {
            def params = [:]

            try {
                params.urn = new URNImpl(pathTokens.urn)
                params.sdate = request.queryParams.sdate ? ISODateUtils.toISODate(request.queryParams.sdate) : null
                params.edate = request.queryParams.edate ? ISODateUtils.toISODate(request.queryParams.edate) : null
            } catch (Exception e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    promise { Fulfiller fulfiller ->
                        Thread.start {
                            fulfiller.success(
                                    deltaService.delta(params.urn, params.sdate, params.edate)
                            )
                        }
                    }
            ) subscribe { result ->
                render json(result)
            }
        }
    }
}