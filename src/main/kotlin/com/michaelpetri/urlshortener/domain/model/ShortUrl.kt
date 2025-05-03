package com.michaelpetri.urlshortener.domain.model

import com.michaelpetri.urlshortener.domain.value.ShortId
import java.net.URL

data class ShortUrl(
    val id: ShortId,
    val destination: URL,
)
