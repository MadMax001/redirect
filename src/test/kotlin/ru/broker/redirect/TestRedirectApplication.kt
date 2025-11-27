package ru.broker.redirect

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<RedirectApplication>().with(TestcontainersConfiguration::class).run(*args)
}
