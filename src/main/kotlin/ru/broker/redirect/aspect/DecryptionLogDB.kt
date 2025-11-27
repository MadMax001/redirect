package ru.broker.redirect.aspect

/**
 * Аннотация для аспекта для записи в БД результата дешифрации ссылки
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DecryptionLogDB
