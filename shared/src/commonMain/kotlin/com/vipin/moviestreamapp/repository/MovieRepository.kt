package com.vipin.moviestreamapp.repository

import com.vipin.moviestreamapp.api.APIClient
import com.vipin.moviestreamapp.models.Movie


class MovieRepository(
    private val apiClient: APIClient
) {

    suspend fun getMovies(): Result<List<Movie>> {
        return try {
            val movies = apiClient.getMovies()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovie(id: String): Result<Movie> {
        return try {
            val movie = apiClient.getMovie(id)
            Result.success(movie)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




