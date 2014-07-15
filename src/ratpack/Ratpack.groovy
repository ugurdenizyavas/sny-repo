import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.process.ProcessIdImpl
import com.sony.ebs.octopus3.commons.urn.URNCreationException
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.MonitoringService
import com.sony.ebs.octopus3.microservices.reposervice.business.Operation
import com.sony.ebs.octopus3.microservices.reposervice.business.OperationEnum
import com.sony.ebs.octopus3.microservices.reposervice.business.OpsParser
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sony.ebs.octopus3.microservices.reposervice.business.upload.RepoUploadEnum
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
ServerErrorHandler defaultErrorHandler = [
        error: { Context context, Exception exception ->
            log.error "error", exception
            exception.printStackTrace()
            context.render "sorry"
        }
] as ServerErrorHandler

ratpack {

    RepoService repoService
    DeltaService deltaService
    MonitoringService monitoringService

    bindings {
        add new JacksonModule()

        bind ServerErrorHandler, defaultErrorHandler
        init {
            RxRatpack.initialize()
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class)
            ctx.beanFactory.registerSingleton "launchConfig", launchConfig

            repoService = ctx.getBean RepoService.class
            deltaService = ctx.getBean DeltaService.class
            monitoringService = ctx.getBean MonitoringService.class

            Jackson
        }
    }

    handlers {
        get("repository") {
            render json(status: 200, message: "Welcome to Repo Service")
        }

        get("repository/healthCheck") {
            def params = [:]

            params.enabled = request.queryParams.enabled

            if (params.enabled) {
                def action = params.enabled.toBoolean()
                if (action) {
                    monitoringService.up()
                    response.status(200)
                    render json(status: 200, message: "App is up for the eyes of LB!")
                } else {
                    monitoringService.down()
                    response.status(200)
                    render json(status: 200, message: "App is down for the eyes of LB!")
                }
            } else {
                if (monitoringService.checkStatus()) {
                    response.status(200)
                    render json(status: 200, message: "Ticking!")
                } else {
                    response.status(404)
                    render json(status: 404, message: "App is down!")
                }
            }
        }

        get("repository/healthCheck/down") {
            monitoringService.down()
            render json(status: 200, message: "App is down for the eyes of LB!")
        }

        get("repository/healthCheck/up") {
            monitoringService.up()
            render json(status: 200, message: "App is up for the eyes of LB!")
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

                get() {
                    def params = [:]

                    try {
                        params.urn = new URNImpl(pathTokens.urn)
                        params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
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
                        params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
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
                        render json(status: 202, deletedFiles: result.filesTracked.collect {
                            it.toString()
                        }, failedFiles: result.filesFailed.collect { it.toString() })
                    }

                }
            }
        }

        //OPS
        post("repository/ops") {
            def params = [:]

            params.recipe = request.body.bytes
            params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()

            if (!params.recipe) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        OpsParser.parse(new String(params.recipe)).each { Operation operation ->
                            def parameters = operation.parameters
                            switch (operation.methodName) {
                                case OperationEnum.ZIP:
                                    repoService.zip new URNImpl(parameters.get("source"))
                                    break
                                case OperationEnum.UPLOAD:
                                    repoService.upload new URNImpl(parameters.get("source")), RepoUploadEnum.valueOf(parameters.get("destination"))
                                    break
                                case OperationEnum.COPY:
                                    repoService.copy new URNImpl(parameters.get("source")), new URNImpl(parameters.get("destination"))
                                    break
                            }
                        }
                    }
            ).subscribe(([
                    onCompleted: {
                    },
                    onNext     : { result ->
                        //unparsable json returns groovy NullObject so we need to check null object
                        if (result.equals(null)) {
                            response.status(400)
                            render json(
                                    status: 400
                            )
                        } else {
                            response.status(200)
                            render json(
                                    status: 200
                            )
                        }
                    },
                    onError    : {
                        Exception e ->
                            response.status(404)
                            render json([status: 404, message: e.message])
                    }
            ] as Subscriber))
        }

        get("repository/zip/:urn") {
            final def ZIP_EXTENSION = ".zip"
            def params = [:]
            try {
                params.urn = new URNImpl(pathTokens.urn)
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
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
                        response.status(201)
                        render json(
                                status: 201,
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

        get("repository/copy/source/:source/destination/:destination") {
            def params = [:]

            try {
                params.sourceStr = new URNImpl(pathTokens.source)
                params.destinationStr = new URNImpl(pathTokens.destination)
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            } catch (URNCreationException e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        repoService.copy params.sourceStr, params.destinationStr
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
        }

        get("repository/upload/source/:source/destination/:destination") {
            def params = [:]

            try {
                params.sourceUrn = new URNImpl(pathTokens.source)
                params.destination = RepoUploadEnum.valueOf(pathTokens.destination)
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
            } catch (Exception e) {
                response.status(400)
                render json(status: 400, message: "rejected")
            }

            observe(
                    blocking {
                        repoService.upload params.sourceUrn, params.destination
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
        }

        //Delta Service
        get("repository/delta/:urn") {
            def params = [:]

            try {
                params.urn = new URNImpl(pathTokens.urn)
                params.sdate = request.queryParams.sdate ? ISODateUtils.toISODate(request.queryParams.sdate) : null
                params.edate = request.queryParams.edate ? ISODateUtils.toISODate(request.queryParams.edate) : null
                params.processId = request.queryParams.processId ? new ProcessIdImpl(request.queryParams.processId) : new ProcessIdImpl()
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
