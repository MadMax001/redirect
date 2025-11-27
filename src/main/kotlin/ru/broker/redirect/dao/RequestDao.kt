package ru.broker.redirect.dao

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.broker.redirect.model.Request
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Repository
class RequestDao(
    private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    /**
     * Сохранить в БД факт перенаправления
     * @param request - детали по перенаправлению
     * @return - id записи в БД. В случае неуспешного сохранения возвращает null
     */
    @Transactional
    @Async("taskExecutor")
    fun save(request: Request): CompletableFuture<UUID?> {
        val sql = "INSERT INTO request (url, request_id) VALUES (:url, :requestId) RETURNING id"
        val keyHolder = GeneratedKeyHolder()
        val params = MapSqlParameterSource()
            .addValue("url", request.url)
            .addValue("requestId", request.requestId)

        jdbcTemplate.update(sql, params, keyHolder)
        val id = keyHolder.keys?.get("id") as UUID
        return CompletableFuture.completedFuture<UUID?>(id)
            .thenApply{ id ->
                logger.debug("Запись для [{}] сохранена в БД: {}", request.requestId, id)
                id
            }
    }
}
