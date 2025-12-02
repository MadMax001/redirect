package ru.broker.redirect.config

class Constants {
    companion object {
        const val LOG_SUCCESS_REDIRECT = "Перенаправление на"
        const val LOG_AUTHENTIFICATION_ERROR_REDIRECT = "Ошибка аутентификации"
        const val DEV_AUTHENTIFICATION_ERROR_ANSWER = "Запрос на несуществующий адрес. Перенаправление на"
        const val LOG_ERROR_REDIRECT = "Ошибка в процессе обработки. Ссылка по умолчанию"
        const val DEV_ERROR_ANSWER = "Ошибка в процессе обработки. Перенаправление на"

        const val LOG_SUCCESS_DB_SAVE = "Запись для сохранена в БД"
        const val LOG_ERROR_DB_SAVE = "Ошибка при записи в БД"

        const val START_REDIRECT_PROCESS = "Запрос на перенаправление"
        const val FINISH_REDIRECT_PROCESS = "Ссылка на"
        const val DECRYPTION_ERROR_TEXT = "Не удалось расшифровать ссылку"
    }
}
