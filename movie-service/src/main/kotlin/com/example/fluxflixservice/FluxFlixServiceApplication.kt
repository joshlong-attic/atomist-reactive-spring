package com.example.fluxflixservice

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.HttpSecurity
import org.springframework.security.core.userdetails.MapUserDetailsRepository
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.util.*

@SpringBootApplication
class FluxFlixServiceApplication {

    @Bean
    fun runner(mr: MovieRepository) = ApplicationRunner {
        val movies = Flux.just("Silence of the Lambdas", "AEon Flux", "Back to the Future")
                .flatMap { mr.save(Movie(title = it)) }
        mr
                .deleteAll()
                .thenMany(movies)
                .thenMany(mr.findAll())
                .subscribe({ println(it) })
    }
}

@Service
class MovieService(private val mr: MovieRepository) {

    fun all() = mr.findAll()

    fun byId(id: String) = mr.findById(id)

    fun events(id: String) = Flux
            .generate({ sink: SynchronousSink<MovieEvent> -> sink.next(MovieEvent(id, Date())) })
            .delayElements(Duration.ofSeconds(1L))
}

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

    @Bean
    fun authentication() =
            MapUserDetailsRepository(User.withUsername("springrod").password("pw").roles("ADMIN", "USER").build(),
                    User.withUsername("starbuxman").password("pw").roles("USER").build())

    @Bean
    fun authorization(http: HttpSecurity) =
            http
                    .httpBasic()
                    .and()
                    .authorizeExchange().anyExchange().hasRole("ADMIN")
                    .and()
                    .build()
}

@Configuration
class WebConfiguration(val ms: MovieService) {

    @Bean
    fun routes() = router {
        GET("/movies") { ok().body<Movie>(ms.all()) }
        GET("/movies/{id}") { ok().body<Movie>(ms.byId(it.pathVariable("id"))) }
        GET("/movies/{id}/events") {
            ok().contentType(MediaType.TEXT_EVENT_STREAM).body<MovieEvent>(ms.events(it.pathVariable("id")))
        }
    }
}

/*
@RestController
class MovieRestController(var ms: MovieService) {

    @GetMapping("/movies")
    fun all() = ms.all()

    @GetMapping("/movies/{id}")
    fun byId(@PathVariable id: String) = ms.byId(id)

    @GetMapping("/movies/{id}/events", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    fun events(@PathVariable id: String) = ms.events(id)
}
*/

data class MovieEvent(var movieId: String? = null, var date: Date? = null)

fun main(args: Array<String>) {
    SpringApplication.run(FluxFlixServiceApplication::class.java, *args)
}


interface MovieRepository : ReactiveMongoRepository<Movie, String>

@Document
data class Movie(@Id var id: String? = null, var title: String? = null)
