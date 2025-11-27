package ru.broker.redirect.controller.prod

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.broker.redirect.service.Redirector

@RestController
@RequestMapping("/v1")
@Suppress("unused")
@Profile("prod")
class MainControllerProd(
    private val redirector: Redirector
) {

    /**
     * end-point для перенаправления клиентов партнерам
     * @param encryptedUrl - зашифрованный url для перенаправления
     * @param iv - инициализационный вектор для дешифрации
     * @param gpbIdString - id от ГПБ
     * @return представление вида RedirectView для редиректа
     */
    @GetMapping("/redirect")
    fun handleRedirect(
        @RequestParam(name = "p1", required = false) encryptedUrl: String?,
        @RequestParam(name = "p2", required = false) iv: String?,
        @RequestParam(name = "p3", required = false) gpbIdString: String?
    ): RedirectView {
        val redirectUrl = redirector.buildRedirectUrl(encryptedUrl, iv, gpbIdString)
        return RedirectView(redirectUrl)
    }

}

