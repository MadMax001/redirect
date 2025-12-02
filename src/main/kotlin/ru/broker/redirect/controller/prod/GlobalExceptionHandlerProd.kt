package ru.broker.redirect.controller.prod

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.view.RedirectView
import ru.broker.redirect.config.Constants.Companion.LOG_ERROR_REDIRECT

@ControllerAdvice
@Suppress("unused")
@Profile("prod")
class GlobalExceptionHandlerProd (@Value("\${redirect.errorUrl}") private val errorUrl: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(request: HttpServletRequest?, ex: Exception): RedirectView {
        logger.error("$LOG_ERROR_REDIRECT, ${request?.requestURI}.", ex)
        return RedirectView(errorUrl)
    }

}
