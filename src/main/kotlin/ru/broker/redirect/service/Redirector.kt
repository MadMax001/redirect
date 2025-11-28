package ru.broker.redirect.service

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.broker.redirect.aspect.DecryptionLogDB
import java.util.UUID

/**
 * Расшифровка и логирование ссылки для перенаправления
 */
@Service
class Redirector (private val encryptor: SymmetricEncryptor) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Расшифровать ссылку. Если ссылку расшифровать не удается, то возникает RuntimeException
     * request - объект HttpServletRequest. Ожидается, что данная функция обрабатывает запросы, содержащие 3 параметра:
     * p1 - ссылка в зашифрованном виде
     * p2 - инициализационный вектор, используется для расшифровки
     * p3 - строковое представление внешнего идентификатора запроса от ГПБ
     */
    @DecryptionLogDB
    fun buildRedirectUrl(request: HttpServletRequest?) : String {
        val encryptedUrl : String? = request?.getParameter("p1")
        val iv : String? = request?.getParameter("p2")
        val gpbIdString : String? = request?.getParameter("p3")
        logger.info("[$gpbIdString]. Запрос на перенаправление")
        val gpbId = safeBuildUUID(gpbIdString)
        val redirectUrl = encryptor.decryptUrl(encryptedUrl, iv)
            ?: throw RuntimeException("[$gpbId]. Не удалось расшифровать ссылку")
        logger.info("[$gpbIdString]. Ссылка на $redirectUrl")
        return redirectUrl
    }

    private fun safeBuildUUID (gpbIdString: String?): UUID? {
        return gpbIdString?.let {
            try {
                UUID.fromString(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}