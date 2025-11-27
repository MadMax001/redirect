package ru.broker.redirect.service

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
     * @param encryptedUrl - ссылка в зашифрованном виде
     * @param iv - инициализационный вектор, используется для расшифровки
     * @param gpbIdString - строковое представление внешнего идентификатора запроса от ГПБ
     */
    @DecryptionLogDB
    fun buildRedirectUrl(encryptedUrl: String?, iv: String?, gpbIdString: String?) : String {
        logger.info("[$gpbIdString]. Запрос на перенаправление")
        val gpbId = safeBuildUUID(gpbIdString)
        val redirectUrl = encryptor.decryptUrl(encryptedUrl, iv)
            ?: throw RuntimeException("[$gpbId]. Не удалось расшифровать ссылку")
        logger.info("[$gpbId]. Ссылка на на $redirectUrl")
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