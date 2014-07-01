package com.sony.ebs.octopus3.microservices.reposervice

import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import ratpack.launch.LaunchConfig

@Configuration
@ComponentScan(value = "com.sony.ebs.octopus3.microservices.reposervice")
@PropertySource(['classpath:/default.properties', 'classpath:/${ENV}.properties'])
class SpringConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    def static setLaunchConfig(ApplicationContext applicationContext, LaunchConfig launchConfig) {
        applicationContext.getBean(RepoService.class)?.launchConfig = launchConfig
        applicationContext.getBean(DeltaService.class)?.launchConfig = launchConfig
    }

}
