package ru.broker.redirect.controller.prod

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
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
     * @param request - объект HttpServletRequest. Ожидается, что данная функция обрабатывает запросы, содержащие 3 параметра:
     * p1 - ссылка в зашифрованном виде
     * p2 - инициализационный вектор, используется для расшифровки
     * p3 - строковое представление внешнего идентификатора запроса от ГПБ
     * @return представление вида RedirectView для редиректа
     */
    @GetMapping("/redirect")
    fun handleRedirect(request: HttpServletRequest): RedirectView {
        val redirectUrl = redirector.buildRedirectUrl(request)
        return RedirectView(redirectUrl)
    }

}

