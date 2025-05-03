package com.michaelpetri.urlshortener.domain.exception

class ShortUrlNotSaved(
    cause: Throwable? = null,
) : RuntimeException(
        "Failed to save short url",
        cause,
    )
