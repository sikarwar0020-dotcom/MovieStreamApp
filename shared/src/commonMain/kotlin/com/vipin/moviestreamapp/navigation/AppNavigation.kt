package com.vipin.moviestreamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vipin.moviestreamapp.api.APIClient
import com.vipin.moviestreamapp.models.MovieIdModel
import com.vipin.moviestreamapp.repository.MovieRepository
import com.vipin.moviestreamapp.screens.MovieDetailsScreen
import com.vipin.moviestreamapp.screens.MoviesListScreen
import com.vipin.moviestreamapp.viewmodels.MovieViewModel


@Composable
fun AppNavigation(){

    val navController = rememberNavController()

    val apiClient = remember { APIClient() }

    val repository = remember {
        MovieRepository(apiClient)
    }
    val viewModel = remember {
        MovieViewModel(repository)
    }

    NavHost(
        navController = navController,
        startDestination = "moviesList"
    ) {
        composable("moviesList") {
            MoviesListScreen(navController,viewModel)
        }

        composable<MovieIdModel> { backStackEntry->
            val movieId = backStackEntry.toRoute<MovieIdModel>()

            MovieDetailsScreen(
                movie = movieId.id,
                navController,
                viewModel
            )
        }
    }

}