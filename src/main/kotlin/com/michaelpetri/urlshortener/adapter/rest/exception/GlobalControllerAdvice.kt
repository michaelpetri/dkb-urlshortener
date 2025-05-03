package com.michaelpetri.urlshortener.adapter.rest.exception

import com.michaelpetri.urlshortener.adapter.rest.dto.ErrorResponse
import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotFound
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class GlobalControllerAdvice {
    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun shortUrlNotFoundHandler(e: WebExchangeBindException): ErrorResponse {
        val details = mutableMapOf<String, MutableList<String>>()

        for (fieldError in e.fieldErrors) {
            val errors = details.getOrPut(fieldError.field) { mutableListOf<String>() }
            errors.add(fieldError.defaultMessage ?: "Unknown violation error")
        }

        return ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            HttpStatus.UNPROCESSABLE_ENTITY.name,
            "Failed to validate request.",
            details,
        )
    }

    @ExceptionHandler(ShortUrlNotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun shortUrlNotFoundHandler() =
        ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.name,
            "The requested short url was not found.",
        )
}
