package ru.broker.redirect.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import ru.broker.redirect.config.Constants.Companion.DEV_AUTHENTIFICATION_ERROR_ANSWER
import ru.broker.redirect.config.Constants.Companion.LOG_AUTHENTIFICATION_ERROR_REDIRECT

@Configuration
@Profile("dev")
class SecurityDevConfiguration {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    @Suppress("unused")
    fun authenticationEntryPoint(
        @Value("\${redirect.errorUrl}") errorUrl: String
    ): AuthenticationEntryPoint =
        object : AuthenticationEntryPoint {
            override fun commence (
                request: HttpServletRequest?,
                response: HttpServletResponse?,
                authException: AuthenticationException?
            ) {
                logger.info("$LOG_AUTHENTIFICATION_ERROR_REDIRECT, ${request?.requestURI}. ${authException?.message}")
                response?.let { resp ->
                    resp.contentType = "text/plain"
                    val writer = response.getWriter()
                    writer.use {
                        it.write("$DEV_AUTHENTIFICATION_ERROR_ANSWER $errorUrl")
                    }
                }
            }

        }
}