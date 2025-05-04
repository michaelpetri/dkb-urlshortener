package com.michaelpetri.urlshortener.infrastructure.cache

import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotFound
import com.michaelpetri.urlshortener.domain.value.ShortId
import com.michaelpetri.urlshortener.infrastructure.jdbc.ShortUrlDatabaseRepository
import com.redis.testcontainers.RedisContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.cache.CacheManager
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import kotlin.test.assertEquals

@SpringBootTest
@Testcontainers
class ShortUrlRedisRepositoryTest {
    @Autowired
    lateinit var repository: ShortUrlCacheRepository

    @Autowired
    lateinit var cacheManager: CacheManager

    @MockitoSpyBean
    lateinit var next: ShortUrlDatabaseRepository

    @Test
    fun `should forward URL to next repository`() {
        // Arrange
        val originalUrl = URI("https://example.tld/foo").toURL()

        // Act
        val shortId = repository.save(originalUrl)
        val retrieved = repository.get(shortId)

        // Assert
        verify(next).save(originalUrl)

        assertEquals(shortId, retrieved.id)
        assertEquals(originalUrl, retrieved.destination)
    }

    @Test
    fun `should get from next repository on cache miss`() {
        // Arrange
        val originalUrl = URI("https://example.tld/foo").toURL()
        val shortId = next.save(originalUrl)

        // Act
        val retrieved = repository.get(shortId)

        // Assert
        verify(next).get(shortId)

        assertEquals(shortId, retrieved.id)
        assertEquals(originalUrl, retrieved.destination)
    }

    @Test
    fun `should not get from next repository on cache hit`() {
        // Arrange
        val originalUrl = URI("https://example.tld/foo").toURL()
        val shortId = next.save(originalUrl)

        cacheManager
            .getCache(CACHE_NAME)!!
            .put(shortId.value, originalUrl.toString())

        // Act
        val retrieved = repository.get(shortId)

        // Assert
        verify(next, never()).get(shortId)

        assertEquals(shortId, retrieved.id)
        assertEquals(originalUrl, retrieved.destination)
    }

    @Test
    fun `should forward exception from next when URL not found`() {
        // Arrange
        val nonExistentId = ShortId(999999UL)

        // Act & Assert
        assertThrows<ShortUrlNotFound> {
            repository.get(nonExistentId)
        }
        verify(next).get(nonExistentId)
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

        private const val CACHE_NAME = "short_url"
    }
}
