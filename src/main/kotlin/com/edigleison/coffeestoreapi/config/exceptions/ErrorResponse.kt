package com.edigleison.coffeestoreapi.config.exceptions

import org.springframework.http.HttpStatus

class ErrorResponse {
    var status: HttpStatus
    var message: String
    var errors: List<String?>? = null

    constructor(status: HttpStatus, message: String, errors: List<String?>?) : super() {
        this.status = status
        this.message = message
        this.errors = errors
    }

    constructor(status: HttpStatus, message: String, error: String?) : super() {
        this.status = status
        this.message = message
        this.errors = listOf(error)
    }
}
