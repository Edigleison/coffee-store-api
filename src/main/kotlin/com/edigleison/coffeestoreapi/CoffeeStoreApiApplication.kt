package com.edigleison.coffeestoreapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class CoffeeStoreApiApplication

fun main(args: Array<String>) {
    runApplication<CoffeeStoreApiApplication>(*args)
}
