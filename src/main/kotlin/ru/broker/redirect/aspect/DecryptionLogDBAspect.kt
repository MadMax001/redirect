package ru.broker.redirect.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.model.Request
import java.util.UUID

/**
 * Логика аспекта для аннотации @DecryptionLogDB
 */
@Aspect
@Component
@Suppress("unused")
class DecryptionLogDBAspect(
    private val dao: RequestDao,
    @Value("\${redirect.errorUrl}") private val errorUrl: String
) {

    /**
     * Прокси-обертка вокруг метода расшифровки ссылки
     */
    @Around("@annotation(ru.broker.redirect.aspect.DecryptionLogDB)")
    fun decryptUrlAroundWorker(joinPoint: ProceedingJoinPoint): Any {
        val result = try {
            joinPoint.proceed()
        } catch (ignore: RuntimeException) {
            null
        }
        val args = joinPoint.args
        val url = result as? String ?: errorUrl
        logInDb(args, url)
        return url
    }

    private fun logInDb(args: Array<Any>, url: String) {
        val requestId = extractBrokerIdArgs(args)
        val request = Request(url, requestId)
        dao.save(request)
    }

    private fun extractBrokerIdArgs(args: Array<Any>): UUID? {
        return if (args.size == 3 && args[2] is String)
            try {
                UUID.fromString(args[2] as String)
            } catch (_: IllegalArgumentException) {
                null
            }
        else
            null
    }
}