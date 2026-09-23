package com.vipin.moviestreamapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vipin.moviestreamapp.models.Movie
import com.vipin.moviestreamapp.player.VideoPlayer
import com.vipin.moviestreamapp.utils.ApiState
import com.vipin.moviestreamapp.viewmodels.MovieViewModel

@Composable
fun MovieDetailsScreen(movie: String, navController: NavController, viewModel: MovieViewModel) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Movies Details Screen")
        }, navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        })
    }) { paddingValues ->
        LaunchedEffect(Unit) {
            viewModel.getMovie(movie)
        }

        val state = viewModel.stateMovie.collectAsState()


        when (val currentState = state.value) {
            is ApiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Something went wrong!")

                }
            }

            ApiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }

            is ApiState.Success -> {
                val data = currentState.data
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    VideoPlayer(
                        videoUrl = data.url,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        adVideoUrl = "https://lorem.video/720p",
                        adPositionSeconds = 10L,
                        autoPlay = true
                    )

                    Text(
                        text = data.title, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

                    )

                    Text(
                        text = "Release on: "+ data.year, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

                    )
                }


            }
        }


    }
}