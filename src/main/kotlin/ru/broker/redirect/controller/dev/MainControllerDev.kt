package ru.broker.redirect.controller.dev

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.broker.redirect.config.Constants
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
     * @param request - объект HttpServletRequest. Ожидается, что данная функция обрабатывает запросы, содержащие 3 параметра:
     * p1 - ссылка в зашифрованном виде
     * p2 - инициализационный вектор, используется для расшифровки
     * p3 - строковое представление внешнего идентификатора запроса от ГПБ
     * @return текстовое сообщение с расшифрованной ссылкой
     */
    @GetMapping("/redirect")
    fun handleRedirect(request: HttpServletRequest): String {
        val redirectUrl = redirector.buildRedirectUrl(request)
        val message = "[${request.getParameter("p3")}]. ${Constants.Companion.LOG_SUCCESS_REDIRECT} $redirectUrl"
        return message
    }

}