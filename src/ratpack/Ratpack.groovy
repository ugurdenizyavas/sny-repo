import com.sony.ebs.octopus3.commons.ratpack.handlers.ErrorHandler
import com.sony.ebs.octopus3.commons.ratpack.handlers.HealthCheckHandler
import com.sony.ebs.octopus3.commons.ratpack.monitoring.MonitoringService
import com.sony.ebs.octopus3.microservices.reposervice.SpringConfig
import com.sony.ebs.octopus3.microservices.reposervice.handlers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.jackson.JacksonModule
import ratpack.rx.RxRatpack

import static ratpack.groovy.Groovy.ratpack

Logger log = LoggerFactory.getLogger("ratpack");

ratpack {

    HealthCheckHandler healthCheckHandler
    OpsHandler opsHandler
    ZipHandler zipHandler
    CopyHandler copyHandler
    UploadHandler uploadHandler
    DeltaHandler deltaHandler
    SaveHandler saveHandler
    ReadHandler readHandler
    DeleteHandler deleteHandler
    FileAttributesHandler fileAttributesHandler

    bindings {
        add new JacksonModule()
        bind ClientErrorHandler, new ErrorHandler()
        bind ServerErrorHandler, new ErrorHandler()

        init {
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class)
            ctx.beanFactory.registerSingleton "launchConfig", launchConfig

            healthCheckHandler = new HealthCheckHandler(monitoringService: new MonitoringService())
            opsHandler = ctx.getBean OpsHandler.class
            zipHandler = ctx.getBean ZipHandler.class
            copyHandler = ctx.getBean CopyHandler.class
            uploadHandler = ctx.getBean UploadHandler.class
            deltaHandler = ctx.getBean DeltaHandler.class
            saveHandler = ctx.getBean SaveHandler.class
            readHandler = ctx.getBean ReadHandler.class
            deleteHandler = ctx.getBean DeleteHandler.class
            fileAttributesHandler = ctx.getBean FileAttributesHandler.class

            RxRatpack.initialize()
        }
    }

    handlers {
        get("healthcheck", healthCheckHandler)
        handler("repository/file/:urn") {
            byMethod {
                post(saveHandler)
                get(readHandler)
                delete(deleteHandler)
            }
        }
        post("repository/ops", opsHandler)
        get("repository/zip/:urn", zipHandler)
        get("repository/copy/source/:source/destination/:destination", copyHandler)
        get("repository/upload/source/:source/destination/:destination", uploadHandler)
        get("repository/delta/:urn", deltaHandler)
        get("repository/fileattributes/:urn", fileAttributesHandler)
    }
}
