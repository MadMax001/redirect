package ru.broker.redirect

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class RedirectApplication

fun main(args: Array<String>) {
	runApplication<RedirectApplication>(*args)
}
