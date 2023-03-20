package com.edigleison.coffeestoreapi.config.exceptions

import com.edigleison.coffeestoreapi.exceptions.NotFound
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.stream.Collectors


@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(RuntimeException::class)
    fun handleAll(exception: RuntimeException, request: WebRequest?): ResponseEntity<ErrorResponse> {
        val message = "Error: " + exception.localizedMessage
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST, message, "error occurred")
        return ResponseEntity(errorResponse, HttpHeaders(), errorResponse.status)
    }

    @ExceptionHandler(NotFound::class)
    fun handleNotFound(exception: NotFound, request: WebRequest?): ResponseEntity<ErrorResponse> {
        val message = "Error: " + exception.localizedMessage
        val errorResponse = ErrorResponse(HttpStatus.NOT_FOUND, message, "error occurred")
        return ResponseEntity(errorResponse, HttpHeaders(), errorResponse.status)
    }

    override fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorList: MutableList<String> = exception.bindingResult.fieldErrors.stream()
            .map { fieldError -> fieldError.defaultMessage }
            .collect(Collectors.toList())
        val errorDetails = ErrorResponse(HttpStatus.BAD_REQUEST, exception.localizedMessage, errorList)

        return handleExceptionInternal(exception, errorDetails, headers, errorDetails.status, request)
    }
}
