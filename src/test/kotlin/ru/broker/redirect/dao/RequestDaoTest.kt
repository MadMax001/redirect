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
        val gpbId = UUID.randomUUID()
        val clientIp = "10.20.30.40"
        val clientAgent = "client-agent"
        val request = Request(url, gpbId, clientIp, clientAgent)

        val id = requestDao.save(request)

        assertThat(id).isNotNull()

        val savedRequest = jdbcTemplate.queryForMap(
            "SELECT url, created_at, gpb_id, client_ip, client_agent FROM request WHERE id=:id",
            mapOf("id" to id)
        )

        assertThat(savedRequest).isNotNull()
        assertThat(savedRequest["url"]).isEqualTo(url)
        assertThat(savedRequest["created_at"]).isNotNull()
        assertThat(savedRequest["gpb_id"]).isEqualTo(gpbId)
        assertThat(savedRequest["client_ip"]).isEqualTo(clientIp)
        assertThat(savedRequest["client_agent"]).isEqualTo(clientAgent)

    }
}
