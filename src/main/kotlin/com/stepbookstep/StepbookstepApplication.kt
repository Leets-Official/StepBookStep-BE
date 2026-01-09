package com.stepbookstep

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class StepbookstepApplication

fun main(args: Array<String>) {
    runApplication<StepbookstepApplication>(*args)
}
