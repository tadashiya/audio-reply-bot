package com.haitekuya.demo002

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import retrofit2.http.POST
import reactor.core.publisher.Flux
import retrofit2.http.GET
import org.springframework.web.reactive.function.server.RouterFunction
import java.io.IOException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.lang.RuntimeException


@RestController
class Demo2Controller {

    @GetMapping("/test")
    fun getTest(): Mono<DemoResponse> {

        return DemoResponse("dsdsds").toMono()
    }

    @GetMapping("/test2")
    fun getTest2(): Mono<DemoResponse> {
        throw RuntimeException()
    }
}

data class DemoResponse(val message: String)

@Configuration
class DemoConfiguration {

    @Bean
    fun getObjectMapper(): ObjectMapper =
            ObjectMapper().registerModule(KotlinModule())
}

@ControllerAdvice
class DemoControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    fun exceptionHandler(exception: Exception) {

    }


}

