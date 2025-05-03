package com.michaelpetri.urlshortener

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.shouldBe
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.BodyContentSpec.containsValidationError() =
    this
        .jsonPath("$.status").value<Int> {
            it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
        }
        .jsonPath("$.title").value<String> {
            it shouldBe HttpStatus.UNPROCESSABLE_ENTITY.name
        }
        .jsonPath("$.message").value<String> {
            it shouldBe "Failed to validate request."
        }

fun WebTestClient.BodyContentSpec.containsDetails(vararg details: Pair<String, List<String>>) =
    jsonPath("$.details").value<Map<String, List<String>>> {
        details.forEach { (path, violations) ->
            it shouldContainKey path
            it[path]!! shouldContainAll violations
        }
    }
