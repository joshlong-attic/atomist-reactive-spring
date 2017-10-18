package com.example.fluxflixservice

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */

@RunWith(SpringRunner::class)
@SpringBootTest
class FfsServiceApplicationTests {

    @Autowired
    private val context: ApplicationContext? = null

    private var client: WebTestClient? = null

    private fun robsCredentials() = ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials("rwinch", "password")

    private fun joshsCredentials() = ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials("jlong", "password")

    @Before
    fun setup() {
        val springSecurity = springSecurity()
        val mss: WebTestClient.MockServerSpec<> = WebTestClient
                .bindToApplicationContext(this.context!!)
                .apply(springSecurity!!)
//                .configureClient()
//                .filter(basicAuthentication())
//                .baseUrl("http://localhost:8080/")
//                .build()
//
//        client = WebTestClient
//                .bindToApplicationContext(context!!)
//                .apply<WebTestClient.MockServerSpec<*>>(springSecurity())
//                .configureClient()
//                .filter(basicAuthentication())
//                .baseUrl("http://localhost:8080/")
//                .build()
    }

    @Test
    fun getMoviesWhenNotAuthenticatedThenIsUnauthorized() {
        client!!
                .get()
                .uri("/movies/")
                .exchange()
                .expectStatus().isUnauthorized
    }

    @Test
    fun getMoviesWhenNotAdminThenIsForbidden() {
        client!!
                .get()
                .uri("/movies/")
                .attributes(joshsCredentials())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun getMoviesWhenIsAdminThenIsOk() {
        client!!
                .get()
                .uri("/movies/")
                .attributes(robsCredentials())
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun getMoviesWhenMockNotAdminThenIsForbidden() {
        client!!
                .mutateWith(mockUser())
                .get()
                .uri("/movies/")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun getMoviesWhenMockIsAdminThenIsOk() {
        client!!
                .mutateWith(mockUser().roles("ADMIN"))
                .get()
                .uri("/movies/")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun getUsersRobWhenIsRobThenIsOk() {
        client!!
                .get()
                .uri("/users/rob")
                .attributes(robsCredentials())
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun getUsersRobWhenIsJoshThenIsForbidden() {
        client!!
                .get()
                .uri("/users/rob")
                .attributes(joshsCredentials())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody().isEmpty
    }

    @Test
    fun getUsersRobWhenNotAuthenticatedThenIsUnauthorized() {
        client!!
                .get()
                .uri("/users/rob")
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody().isEmpty
    }

    @Test
    fun getUsersMeWhenIsRobThenIsOk() {
        client!!
                .get()
                .uri("/users/me")
                .attributes(robsCredentials())
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun getUsersMeWhenIsJoshThenIsForbidden() {
        client!!
                .get()
                .uri("/users/me")
                .attributes(joshsCredentials())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun getUsersMeWhenNotAuthenticatedThenIsUnauthorized() {
        client!!
                .get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody().isEmpty
    }

}
