package com.michaelpetri.urlshortener

import com.redis.testcontainers.RedisContainer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class UrlShortenerApplicationTests {
    @Test
    fun contextLoads() {
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
