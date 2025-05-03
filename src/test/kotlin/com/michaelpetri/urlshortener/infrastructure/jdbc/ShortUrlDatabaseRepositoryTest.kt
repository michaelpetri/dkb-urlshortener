package com.michaelpetri.urlshortener.infrastructure.jdbc

import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotFound
import com.michaelpetri.urlshortener.domain.value.ShortId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import kotlin.test.assertEquals

@SpringBootTest
@Testcontainers
class ShortUrlDatabaseRepositoryTest {
    @Autowired
    lateinit var repository: ShortUrlDatabaseRepository

    @Test
    fun `should save and retrieve URL`() {
        // Arrange
        val originalUrl = URI("https://example.tld/foo").toURL()

        // Act
        val shortId = repository.save(originalUrl)
        val retrieved = repository.get(shortId)

        // Assert
        assertEquals(shortId, retrieved.id)
        assertEquals(originalUrl, retrieved.destination)
    }

    @Test
    fun `should throw when URL not found`() {
        // Arrange
        val nonExistentId = ShortId(999999UL)

        // Act & Assert
        assertThrows<ShortUrlNotFound> {
            repository.get(nonExistentId)
        }
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        @Suppress("UNUSED")
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine3.20")
    }
}
