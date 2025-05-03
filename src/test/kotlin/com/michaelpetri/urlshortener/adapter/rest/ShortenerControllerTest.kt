package com.michaelpetri.urlshortener.adapter.rest

import com.michaelpetri.urlshortener.application.config.ShortenerConfig
import com.michaelpetri.urlshortener.containsDetails
import com.michaelpetri.urlshortener.containsValidationError
import com.redis.testcontainers.RedisContainer
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebTestClient
@Testcontainers
class ShortenerControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var config: ShortenerConfig

    @Test
    fun `should shorten URL`() {
        // Arrange
        val originalUrl = "https://example.tld/foo?bar=baz#qux"

        // Act
        val response =
            webTestClient.post()
                .uri("/api/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("url" to originalUrl))
                .exchange()

        // Assert
        response.expectStatus().isOk
            .expectBody()
            .jsonPath("$.url").value<String> {
                it shouldBe "${config.baseUrl}/2"
            }
    }

    @Test
    fun `should fail graceful with malformed URL`() {
        // Arrange
        val originalUrl = "this-is-not-an-url"

        // Act
        val response =
            webTestClient.post()
                .uri("/api/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("url" to originalUrl))
                .exchange()

        // Assert
        response
            .expectStatus().value {
                it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
            }
            .expectBody()
            .containsValidationError()
            .containsDetails(
                "url" to
                    listOf(
                        "must be a valid URL",
                    ),
            )
    }

    @Test
    fun `should fail graceful with empty URL`() {
        // Arrange
        val originalUrl = ""

        // Act
        val response =
            webTestClient.post()
                .uri("/api/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("url" to originalUrl))
                .exchange()

        // Assert
        response
            .expectStatus().value {
                it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
            }
            .expectBody()
            .containsValidationError()
            .containsDetails(
                "url" to
                    listOf(
                        "must not be blank",
                    ),
            )
    }

    @Test
    fun `should fail graceful with null URL`() {
        // Arrange
        val originalUrl = null

        // Act
        val response =
            webTestClient.post()
                .uri("/api/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("url" to originalUrl))
                .exchange()

        // Assert
        response
            .expectStatus().value {
                it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
            }
            .expectBody()
            .containsValidationError()
            .containsDetails(
                "url" to
                    listOf(
                        "must not be null",
                        "must not be blank",
                    ),
            )
    }

    @Test
    fun `should fail graceful with missing URL`() {
        // Arrange
        val body = emptyMap<String, String>()

        // Act
        val response =
            webTestClient.post()
                .uri("/api/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()

        // Assert
        response
            .expectStatus().value {
                it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
            }
            .expectBody()
            .containsValidationError()
            .containsDetails(
                "url" to
                    listOf(
                        "must not be null",
                        "must not be blank",
                    ),
            )
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
