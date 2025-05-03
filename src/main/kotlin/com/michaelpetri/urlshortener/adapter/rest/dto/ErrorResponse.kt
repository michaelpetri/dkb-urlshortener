package com.michaelpetri.urlshortener.adapter.rest.dto

data class ErrorResponse(
    val status: Int,
    val title: String,
    val message: String,
    val details: Map<String, List<String>> = emptyMap(),
)
