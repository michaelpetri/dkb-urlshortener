package com.michaelpetri.urlshortener.adapter.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL

data class GenerateShortUrlRequest(
    @field:NotNull
    @field:NotBlank
    @field:URL
    val url: String? = null,
)
