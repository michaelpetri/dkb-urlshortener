package com.michaelpetri.urlshortener.domain.service

import com.michaelpetri.urlshortener.application.config.ShortenerConfig
import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotSaved
import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import com.michaelpetri.urlshortener.domain.value.ShortId
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.net.URI

class SimpleUrlShortenerTest {
    private val urls = mock<ShortUrlRepository>()
    private val config = ShortenerConfig().apply { baseUrl = "https://short.tld" }
    private val shortener = SimpleUrlShortener(urls, config)

    @Test
    fun `should return shortened URL using base URL and encoded ID`() {
        // Arrange
        val originalUrl = URI("https://example.com/foo").toURL()
        val shortId = ShortId(123UL)

        whenever(urls.save(eq(originalUrl))).thenReturn(shortId)

        // Act
        val result = shortener.invoke(originalUrl)

        // Assert
        result.toString() shouldBe "${config.baseUrl}/${shortId.encode()}"
    }

    @Test
    fun `should fail when repository fails`() {
        // Arrange
        val originalUrl = URI("https://example.com/foo").toURL()

        whenever(urls.save(eq(originalUrl))).thenThrow(ShortUrlNotSaved())

        // Act & Assert
        assertThrows<ShortUrlNotSaved> {
            shortener.invoke(originalUrl)
        }
    }

    @Test
    fun `should fail to initialize with malformed base url`() {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            SimpleUrlShortener(urls, ShortenerConfig().apply { baseUrl = "malformed-url" })
        }
    }
}
