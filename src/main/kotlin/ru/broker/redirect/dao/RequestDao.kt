package ru.broker.redirect.dao

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.broker.redirect.model.Request
import java.util.UUID

@Repository
class RequestDao(
    private val jdbcTemplate: NamedParameterJdbcTemplate) {


    /**
     * Сохранить в БД запрос на перенаправление
     * @param request - детали по перенаправлению
     * @return - id записи в БД. В случае неуспешного сохранения возвращает null
     */
    @Transactional
    fun save(request: Request): UUID? {
        val sql = "INSERT INTO request (url, gpb_id, client_ip, client_agent) VALUES (:url, :gpbId, :clientId, :clientAgent) RETURNING id"
        val keyHolder = GeneratedKeyHolder()
        val params = MapSqlParameterSource()
            .addValue("url", request.url)
            .addValue("gpbId", request.gpbId)
            .addValue("clientId", request.clientIP)
            .addValue("clientAgent", request.clientAgent)

        jdbcTemplate.update(sql, params, keyHolder)
        return keyHolder.keys?.get("id") as? UUID
    }
}
