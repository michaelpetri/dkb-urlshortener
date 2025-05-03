package com.michaelpetri.urlshortener.domain.repository

import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotFound
import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotSaved
import com.michaelpetri.urlshortener.domain.model.ShortUrl
import com.michaelpetri.urlshortener.domain.value.ShortId
import java.net.URL
import kotlin.jvm.Throws

interface ShortUrlRepository {
    @Throws(ShortUrlNotSaved::class)
    fun save(url: URL): ShortId

    @Throws(ShortUrlNotFound::class)
    fun get(id: ShortId): ShortUrl
}
