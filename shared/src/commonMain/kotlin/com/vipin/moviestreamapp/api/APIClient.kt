package com.vipin.moviestreamapp.api

import com.vipin.moviestreamapp.models.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class APIClient {

    private val client: HttpClient = createHttpClient().config {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
        }
    }

    suspend fun getMovies(): List<Movie> {
        return client.get("http://192.168.29.100:8080/api/movies").body()
    }

    suspend fun getMovie(id: String): Movie {
        return client.get("http://192.168.29.100:8080/api/movies/$id").body()
    }

}