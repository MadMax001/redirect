package ru.broker.redirect.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.model.Request
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Сервис-агрегатор для операций с БД
 */
@Service
class DBHandler (
    private val requestDao: RequestDao,
    @Qualifier("taskExecutor") private val taskExecutor: Executor
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Сохранить в БД запрос на перенаправление
     * @param request - детали по перенаправлению
     * @return - id записи в БД. В случае неуспешного сохранения возвращает null
     */

    fun saveRequest(request: Request): CompletableFuture<UUID?> {
        return CompletableFuture.supplyAsync( {requestDao.save(request)}, taskExecutor)
            .thenApply{ id ->
                logger.info("[{}]. Запись для сохранена в БД: {}", request.gpbId, id)
                id
            }.exceptionally { error ->
                logger.error("[{}]. Ошибка при записи в БД. ", request.gpbId, error)
                throw error
            }
    }
}