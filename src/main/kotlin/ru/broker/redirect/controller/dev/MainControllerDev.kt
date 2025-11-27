package ru.broker.redirect.controller.dev

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.broker.redirect.service.Redirector

@RestController
@RequestMapping("/v1")
@Suppress("unused")
@Profile("dev")
class MainControllerDev(
    private val redirector: Redirector
) {

    /**
     * end-point для расшифровки ссылки клиента
     * @param encryptedUrl - зашифрованный url для перенаправления
     * @param iv - инициализационный вектор для дешифрации
     * @param gpbIdString - id от ГПБ
     * @return текстовое сообщение с расшифрованной ссылкой
     */
    @GetMapping("/redirect")
    fun handleRedirect(
        @RequestParam(name = "p1", required = false) encryptedUrl: String?,
        @RequestParam(name = "p2", required = false) iv: String?,
        @RequestParam(name = "p3", required = false) gpbIdString: String?
    ): String {
        val redirectUrl = redirector.buildRedirectUrl(encryptedUrl, iv, gpbIdString)
        val message = "[$gpbIdString]. Перенаправление на $redirectUrl"
        return message
    }

}

