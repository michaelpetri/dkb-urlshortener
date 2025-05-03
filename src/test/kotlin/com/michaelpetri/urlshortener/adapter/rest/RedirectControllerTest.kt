package com.michaelpetri.urlshortener.adapter.rest

import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import com.michaelpetri.urlshortener.domain.value.ShortId
import com.redis.testcontainers.RedisContainer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebTestClient
@Testcontainers
class RedirectControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var shortUrlRepository: ShortUrlRepository

    @Test
    fun `should redirect to the original URL when a valid short URL is provided`() {
        // Arrange
        val originalUrl = URI("https://example.tld/foo?bar=baz").toURL()
        val shortId = shortUrlRepository.save(originalUrl)

        // Act
        val response =
            webTestClient.get()
                .uri("/${shortId.encode()}")
                .exchange()

        // Assert
        response.expectStatus()
            .is3xxRedirection
            .expectHeader()
            .location(originalUrl.toString())
    }

    @Test
    fun `should return 404 if short URL does not exist`() {
        // Arrange
        val invalidShortId = ShortId(9999U)

        // Act
        val response =
            webTestClient.get()
                .uri("/${invalidShortId.value}")
                .exchange()

        // Assert
        response.expectStatus().isNotFound
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        @Suppress("UNUSED")
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine3.20")

        @Container
        @ServiceConnection
        @JvmStatic
        @Suppress("UNUSED")
        private val redis = RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME)
    }
}
