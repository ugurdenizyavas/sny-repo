import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

beans {
    xmlns([ctx:'http://www.springframework.org/schema/context'])
    ctx.'component-scan'('base-package': 'com.sony.ebs.octopus3')

    configurer(PropertySourcesPlaceholderConfigurer) {
        locations = ['classpath:/default.properties', 'classpath:/${environment}.properties']
        ignoreResourceNotFound = true
    }
}


