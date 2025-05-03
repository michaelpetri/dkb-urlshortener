package com.michaelpetri.urlshortener.infrastructure.redis

import com.michaelpetri.urlshortener.domain.model.ShortUrl
import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import com.michaelpetri.urlshortener.domain.value.ShortId
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URL

@Primary
@Service
class ShortUrlRedisRepository(
    private val next: ShortUrlRepository,
    cacheManager: CacheManager,
) : ShortUrlRepository {
    private val cache =
        cacheManager.getCache(CACHE_NAME)
            ?: error("Failed to load cache with name '$CACHE_NAME'")

    override fun save(url: URL): ShortId {
        val shortId = next.save(url)
        val shortUrl = ShortUrl(shortId, url)
        cache.put(shortId.value, shortUrl.destination.toString())

        return shortId
    }

    override fun get(id: ShortId): ShortUrl {
        val cachedUrl = cache.get(id.value, String::class.java)

        if (cachedUrl != null) {
            return ShortUrl(
                id,
                URI(cachedUrl).toURL(),
            )
        }

        return next.get(id).also { cache.put(id, it) }
    }

    companion object {
        private const val CACHE_NAME = "short_url"
    }
}
