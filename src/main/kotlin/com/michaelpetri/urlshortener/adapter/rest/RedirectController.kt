package com.michaelpetri.urlshortener.adapter.rest

import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import com.michaelpetri.urlshortener.domain.value.ShortId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.reactive.result.view.RedirectView

@Controller
class RedirectController(
    private val shortUrls: ShortUrlRepository,
) {
    @GetMapping("/{encodedShortId:\\w+}")
    suspend operator fun invoke(
        @PathVariable encodedShortId: String,
    ): RedirectView {
        val id = ShortId.decode(encodedShortId)
        val shortUrl = shortUrls.get(id)

        return RedirectView(shortUrl.destination.toString(), HttpStatus.TEMPORARY_REDIRECT)
    }
}
