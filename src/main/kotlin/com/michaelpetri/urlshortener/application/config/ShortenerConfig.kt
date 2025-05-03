package com.michaelpetri.urlshortener.application.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "shortener")
class ShortenerConfig {
    lateinit var baseUrl: String
}
