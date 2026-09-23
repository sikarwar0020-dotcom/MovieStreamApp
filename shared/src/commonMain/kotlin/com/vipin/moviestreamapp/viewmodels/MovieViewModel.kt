package com.vipin.moviestreamapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vipin.moviestreamapp.models.Movie
import com.vipin.moviestreamapp.repository.MovieRepository
import com.vipin.moviestreamapp.utils.ApiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ApiState<List<Movie>>>(ApiState.Loading)

    val state: StateFlow<ApiState<List<Movie>>> = _state.asStateFlow()


    private val _stateMovie = MutableStateFlow<ApiState<Movie>>(ApiState.Loading)

    val stateMovie: StateFlow<ApiState<Movie>> = _stateMovie.asStateFlow()


    fun getMovies() {
        viewModelScope.launch {
            _state.value = ApiState.Loading
            repository.getMovies().onSuccess { movies ->
                _state.value = ApiState.Success(movies)
            }.onFailure { exception ->
                _state.value = ApiState.Error(
                    exception.message ?: "Unable to load movies"
                )
            }

        }


    }


    fun getMovie(movieId: String) {
        viewModelScope.launch {
            _stateMovie.value = ApiState.Loading
            repository.getMovie(movieId).onSuccess { movie ->
                _stateMovie.value = ApiState.Success(movie)
            }.onFailure { exception ->
                _stateMovie.value = ApiState.Error(
                    exception.message ?: "Unable to load movies"
                )
            }

        }


    }
}