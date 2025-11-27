package ru.broker.redirect.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
@Suppress("unused")
class AsyncConfiguration {
    @Bean(name = ["taskExecutor"]) 
    fun taskExecutor(): Executor {
        val factory = Thread.ofVirtual().factory()
        return Executors.newThreadPerTaskExecutor(factory)
    }
}
