package com.sony.ebs.octopus3.microservices.reposervice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

@Configuration
@ComponentScan(value = "com.sony.ebs.octopus3.microservices.reposervice")
@PropertySource(ignoreResourceNotFound = true, value = ['classpath:/default.properties', 'classpath:/${environment}.properties'])
class SpringConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        new PropertySourcesPlaceholderConfigurer();
    }

    //TODO: Add amazon upload bean here

}
