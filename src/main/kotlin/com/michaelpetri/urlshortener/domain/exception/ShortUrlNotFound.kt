package com.michaelpetri.urlshortener.domain.exception

import com.michaelpetri.urlshortener.domain.value.ShortId

class ShortUrlNotFound(
    id: ShortId,
    cause: Throwable? = null,
) : RuntimeException(
        "Short url not found by id \"${id.value}\"",
        cause,
    )
