package ru.broker.redirect.aspect

import jakarta.servlet.http.HttpServletRequest
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
    private val gpbIdRequestParamName = "p3"

    /**
     * Прокси-обертка вокруг метода расшифровки ссылки
     */
    @Around("@annotation(ru.broker.redirect.aspect.DecryptionLogDB) && execution(String *(..))")
    fun decryptUrlAroundWorker(joinPoint: ProceedingJoinPoint): Any {
        val result = try {
            joinPoint.proceed()
        } catch (ignore: RuntimeException) {
            null
        }
        val url = result as? String ?: errorUrl
        val args = joinPoint.args
        logInDb(args, url)
        return url
    }

    private fun logInDb(args: Array<Any>, url: String) {
        val httpRequest = extractHttRequest(args)
        val request = buildRequest(httpRequest, url)
        dao.save(request)
    }

    private fun buildRequest(httpRequest: HttpServletRequest?, url: String): Request {
        val requestId = fetchGpbId(httpRequest)
        val clientIp = fetchClientIp(httpRequest)
        val clientAgent = fetchClientAgent(httpRequest)
        return Request(url, requestId, clientIp, clientAgent)
    }

    private fun extractHttRequest(args: Array<Any>): HttpServletRequest? {
        return if (args.isNotEmpty() && args[0] is HttpServletRequest) args[0] as HttpServletRequest else null
    }

    private fun fetchGpbId(httpRequest: HttpServletRequest?): UUID? {
        return httpRequest?.getParameter(gpbIdRequestParamName)?.let {
            try {
                UUID.fromString(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    private fun fetchClientIp(httpRequest: HttpServletRequest?): String? {
        var clientIp = httpRequest?.remoteAddr
        if (clientIp.isNullOrEmpty()) {
            clientIp = fetchHeaderByName(httpRequest, "X-Forwarded-For")
            if (clientIp.isNullOrEmpty())
                return fetchHeaderByName(httpRequest, "X-Real-IP")
        }
        return clientIp
    }

    private fun fetchClientAgent(httpRequest: HttpServletRequest?): String? {
        return fetchWholeHeaderByName(httpRequest, "User-Agent")
    }

    private fun fetchHeaderByName (httpRequest: HttpServletRequest?, header: String): String? {
        return fetchWholeHeaderByName(httpRequest, header)?.split(",")?.first()
    }

    private fun fetchWholeHeaderByName (httpRequest: HttpServletRequest?, header: String): String? {
        return httpRequest?.getHeader(header)
    }

}