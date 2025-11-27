package ru.broker.redirect.controller.dev

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
@Suppress("unused")
@Profile("dev")
class GlobalExceptionHandlerDev (@Value("\${redirect.errorUrl}") private val errorUrl: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseBody
    fun handleNotFound(ex: NoHandlerFoundException): String {
        return "Запрос на несуществующий адрес. Перенаправление на $errorUrl"
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleGenericException(ex: Exception): String {
        logger.error("Ошибка в процессе обработки. Ссылка по умолчанию", ex)
        return "Ошибка в процессе обработки. Перенаправление на $errorUrl"
    }

}
