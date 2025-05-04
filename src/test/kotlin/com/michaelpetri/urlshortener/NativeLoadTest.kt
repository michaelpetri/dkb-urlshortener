package com.michaelpetri.urlshortener

import com.redis.testcontainers.RedisContainer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.average
import kotlin.system.measureTimeMillis

@Tag("load-test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = [
        "shortener.base-url=http://127.0.0.1:8080",
    ],
)
@Testcontainers
class NativeLoadTest {
    @Value("\${shortener.base-url}")
    private lateinit var baseUrl: String

    private val requiredShortUrls = 100_000
    private val concurrencyLevel = 1_000

    private val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            followRedirects = false
        }

    private val successCounter = AtomicInteger(0)
    private val errorCounter = AtomicInteger(0)
    private val responseTimes = mutableListOf<Long>()

    suspend fun createShortUrl(iteration: Int) {
        try {
            val startTime = System.currentTimeMillis()
            val response =
                httpClient.post("$baseUrl/api/short-url") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("url" to "https://example.tld/iteration/$iteration"))
                }
            val endTime = System.currentTimeMillis()
            responseTimes.add(endTime - startTime)
            if (response.status.isSuccess()) {
                successCounter.incrementAndGet()
                val shortUrlResponse: Map<String, String> = response.body()
                val shortUrl = shortUrlResponse["url"]
                if (shortUrl != null) {
                    followShortUrl(shortUrl, iteration)
                }
            } else {
                errorCounter.incrementAndGet()
                println("Error creating short URL: ${response.status}")
            }
        } catch (e: Exception) {
            errorCounter.incrementAndGet()
            println("Failed to create short URL ($iteration): ${e.message}")
        }
    }

    suspend fun followShortUrl(
        shortUrl: String,
        iteration: Int,
    ) {
        try {
            val responseTime =
                measureTimeMillis {
                    val response = httpClient.get(shortUrl)

                    if (response.status != HttpStatusCode.TemporaryRedirect) {
                        errorCounter.incrementAndGet()
                        println("Error following short URL: ${response.status}")
                    } else {
                        successCounter.incrementAndGet()
                    }
                }

            responseTimes.add(responseTime)
        } catch (e: Exception) {
            errorCounter.incrementAndGet()
            println("Failed to follow short URL ($iteration): ${e.message}")
        }
    }

    @Test
    fun runNativeLoadTest(): Unit =
        runBlocking {
            println("--- Load Test Started ---")
            println("\n")

            val duration =
                measureTimeMillis {
                    (1..requiredShortUrls)
                        .asFlow()
                        .onEach {
                            val progress = it.toDouble() / requiredShortUrls * 100
                            print("\rProgress: ${progress.toInt()}%")
                            System.out.flush()
                        }.map { id -> createShortUrl(id) }
                        .buffer(concurrencyLevel)
                        .collect()
                }

            println("\n")
            println("--- Load Test Results ---")
            println("Total Short URLs: $requiredShortUrls")
            println("Successes: ${successCounter.get()}")
            println("Errors: ${errorCounter.get()}")

            if (responseTimes.isNotEmpty()) {
                val averageResponseTime = responseTimes.average()
                val maxResponseTime = responseTimes.max()
                val minResponseTime = responseTimes.min()
                println("Average Response Time: ${String.format("%.2f", averageResponseTime)} ms")
                println("Max Response Time: $maxResponseTime ms")
                println("Min Response Time: $minResponseTime ms")
            }
            println("Total Test Duration: $duration ms")

            errorCounter shouldBeLessThan requiredShortUrls * 0.1
            responseTimes shouldBeAverageLessThan 500
        }

    private companion object {
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
