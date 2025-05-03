package com.michaelpetri.urlshortener.domain.service

import com.michaelpetri.urlshortener.application.config.ShortenerConfig
import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL

fun interface UrlShortener {
    operator fun invoke(url: URL): URL
}

@Service
class SimpleUrlShortener(
    private val urls: ShortUrlRepository,
    config: ShortenerConfig,
) : UrlShortener {
    private val baseUrl = URI(config.baseUrl).toURL()

    override fun invoke(url: URL): URL {
        val id = urls.save(url)

        val uri =
            UriComponentsBuilder.fromUriString(baseUrl.toString())
                .pathSegment(id.encode())
                .build()
                .toUri()

        return uri.toURL()
    }
}
