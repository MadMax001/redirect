package ru.broker.redirect.dao

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.broker.redirect.TestcontainersConfiguration
import ru.broker.redirect.model.Request
import java.util.UUID

@SpringBootTest
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RequestDaoTest {

    @Autowired
    private lateinit var requestDao: RequestDao

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Test
    fun saveReqeustTest() {
        val url = "http://example.com"
        val requestId = UUID.randomUUID()
        val request = Request(url, requestId)

        val idFuture = requestDao.save(request)
        val id = idFuture.get()

        assertThat(id).isNotNull()

        val savedRequest = jdbcTemplate.queryForMap(
            "SELECT url, created_at, request_id FROM request WHERE id=:id",
            mapOf("id" to id)
        )

        assertThat(savedRequest).isNotNull()
        assertThat(savedRequest["url"]).isEqualTo(url)
        assertThat(savedRequest["created_at"]).isNotNull()
        assertThat(savedRequest["request_id"]).isEqualTo(requestId)

    }
}
