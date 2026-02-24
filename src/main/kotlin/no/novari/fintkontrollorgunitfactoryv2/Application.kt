package no.novari.fintkontrollorgunitfactoryv2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan
@SpringBootApplication(
    scanBasePackages = ["no.novari", "no.fintlabs"],
)
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
