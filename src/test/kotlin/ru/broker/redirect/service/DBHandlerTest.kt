package ru.broker.redirect.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.broker.redirect.config.AsyncConfiguration
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.model.Request
import java.util.UUID

@ExtendWith(SpringExtension::class, OutputCaptureExtension::class)
@ContextConfiguration(classes = [DBHandler::class, AsyncConfiguration::class])
class DBHandlerTest {
    private val successRequestLog = "Запись для сохранена в БД"
    private val errorRequestLog = "Ошибка при записи в БД"

    @MockitoBean
    private lateinit var requestDao: RequestDao

    @Autowired
    private lateinit var dbHandler: DBHandler

    @Test
    fun successSaveRequestTest(capturedOutput: CapturedOutput) {
        val gpbId = UUID.randomUUID()
        val dbId = UUID.randomUUID()
        val request = Request("url", gpbId, null, null)
        whenever(requestDao.save(request)).thenReturn(dbId)
        val result = dbHandler.saveRequest(request)

        val id = result.get()
        assertThat(id).isEqualTo(dbId)

        assertThat(capturedOutput.all)
            .contains(successRequestLog)
            .contains(gpbId.toString())
            .contains(dbId.toString())
            .doesNotContain(errorRequestLog)
    }

    @Test
    fun errorSaveRequestTest(capturedOutput: CapturedOutput) {
        val gpbId = UUID.randomUUID()
        val request = Request("url", gpbId, null, null)
        val errorMessage = "Что-то пошло не так"
        val error = RuntimeException(errorMessage)
        doThrow(error).whenever(requestDao).save(any())

        val result = dbHandler.saveRequest(request)

        assertThatThrownBy{ result.get() }
            .hasMessageContaining(errorMessage)
        assertThat(capturedOutput.all)
            .doesNotContain(successRequestLog)
            .contains(errorRequestLog)
            .contains(gpbId.toString())

    }
}