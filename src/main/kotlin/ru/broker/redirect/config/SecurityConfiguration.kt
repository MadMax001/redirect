package ru.broker.redirect.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.filter.CharacterEncodingFilter

@Configuration
class SecurityConfiguration (
    @Qualifier("authenticationEntryPoint") private val authenticationEntryPoint: AuthenticationEntryPoint
) {

    @Bean
    @Suppress("unused")
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val filter = CharacterEncodingFilter().apply {
            encoding = "UTF-8"
            setForceEncoding(true)
        }
        return http
            .authorizeHttpRequests {auth ->
                auth.requestMatchers("/v1/redirect").permitAll()
                auth.requestMatchers("/e67d4c74-dfcd-4c24-94f9-7540d4d07f3b/**").authenticated()
            }
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(authenticationEntryPoint)}
            .httpBasic {  }
            .csrf { configurer -> configurer.disable() }
            .addFilterBefore(filter, BasicAuthenticationFilter::class.java)
            .build()
    }
}