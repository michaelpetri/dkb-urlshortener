package com.michaelpetri.urlshortener.infrastructure.jdbc

import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotFound
import com.michaelpetri.urlshortener.domain.exception.ShortUrlNotSaved
import com.michaelpetri.urlshortener.domain.model.ShortUrl
import com.michaelpetri.urlshortener.domain.repository.ShortUrlRepository
import com.michaelpetri.urlshortener.domain.value.ShortId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URL
import java.sql.ResultSet

@Service
class ShortUrlDatabaseRepository(
    private val db: JdbcTemplate,
) : ShortUrlRepository {
    override fun save(url: URL): ShortId =
        runCatching {
            val id =
                db.queryForObject(
                    "INSERT INTO short_url (destination) VALUES (?) RETURNING id",
                    Long::class.java,
                    url.toString(),
                )!!

            ShortId(id.toULong())
        }.getOrElse {
            throw ShortUrlNotSaved(it)
        }

    override fun get(id: ShortId): ShortUrl =
        runCatching {
            return db.queryForObject(
                "SELECT * FROM short_url WHERE id = ?",
                ShortUrlMapper,
                id.value.toLong(),
            )!!
        }.getOrElse {
            throw ShortUrlNotFound(id, it)
        }

    private object ShortUrlMapper : RowMapper<ShortUrl> {
        override fun mapRow(
            rs: ResultSet,
            rowNum: Int,
        ): ShortUrl =
            ShortUrl(
                ShortId(rs.getLong("id").toULong()),
                URI(rs.getString("destination")).toURL(),
            )
    }
}
