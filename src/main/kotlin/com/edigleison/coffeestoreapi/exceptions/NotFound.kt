package com.edigleison.coffeestoreapi.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NotFound(
    override val message: String
) : RuntimeException(message) {
}
