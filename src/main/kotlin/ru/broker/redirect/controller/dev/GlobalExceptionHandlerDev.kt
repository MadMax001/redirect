package ru.broker.redirect.controller.dev

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import ru.broker.redirect.config.Constants.Companion.DEV_ERROR_ANSWER
import ru.broker.redirect.config.Constants.Companion.LOG_ERROR_REDIRECT

@ControllerAdvice
@Suppress("unused")
@Profile("dev")
class GlobalExceptionHandlerDev (@Value("\${redirect.errorUrl}") private val errorUrl: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleGenericException(request: HttpServletRequest?, ex: Exception): String {
        logger.error("$LOG_ERROR_REDIRECT, ${request?.requestURI}.", ex)
        return "$DEV_ERROR_ANSWER $errorUrl"
    }

}
