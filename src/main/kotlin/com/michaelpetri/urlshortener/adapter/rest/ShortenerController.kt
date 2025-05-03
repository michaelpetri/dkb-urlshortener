package com.michaelpetri.urlshortener.adapter.rest

import com.michaelpetri.urlshortener.adapter.rest.dto.GenerateShortUrlRequest
import com.michaelpetri.urlshortener.adapter.rest.dto.ShortUrlGeneratedResponse
import com.michaelpetri.urlshortener.domain.service.UrlShortener
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ShortenerController(
    private val generateShortUrl: UrlShortener,
) {
    @PostMapping(
        "/api/short-url",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend operator fun invoke(
        @Valid @RequestBody request: GenerateShortUrlRequest,
    ) = ResponseEntity.ok(
        ShortUrlGeneratedResponse(
            generateShortUrl(
                URI(request.url!!).toURL(),
            ),
        ),
    )
}
