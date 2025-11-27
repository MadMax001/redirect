package ru.broker.redirect.controller.prod

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.view.RedirectView

@ControllerAdvice
@Suppress("unused")
@Profile("prod")
class GlobalExceptionHandlerProd (@Value("\${redirect.errorUrl}") private val errorUrl: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(ex: NoHandlerFoundException): RedirectView {
        return RedirectView(errorUrl)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): RedirectView {
        logger.error("Ошибка в процессе обработки. Ссылка по умолчанию", ex)
        return RedirectView(errorUrl)
    }

}
