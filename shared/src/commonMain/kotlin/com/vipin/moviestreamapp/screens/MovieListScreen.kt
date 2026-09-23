package com.vipin.moviestreamapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seiko.imageloader.rememberImagePainter
import com.vipin.moviestreamapp.api.APIClient
import com.vipin.moviestreamapp.models.Movie
import com.vipin.moviestreamapp.models.MovieIdModel
import com.vipin.moviestreamapp.theme.LIGHT_GREY
import com.vipin.moviestreamapp.utils.ApiState
import com.vipin.moviestreamapp.viewmodels.MovieViewModel
import moviestreamapp.shared.generated.resources.Res
import moviestreamapp.shared.generated.resources.cow
import org.jetbrains.compose.resources.painterResource


@Composable
fun MoviesListScreen(
    navController: NavController, viewModel: MovieViewModel
) {


    LaunchedEffect(Unit) {
        viewModel.getMovies()

    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text("Movies List")
            })
    }) { paddingValues ->

        val state = viewModel.state.collectAsState()

        when (val currentState = state.value) {

            ApiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }

            is ApiState.Success -> {
                val movies = currentState.data
                LazyColumn(Modifier.padding(paddingValues)) {
                    items(
                        items = movies,
                    ) { movie ->

                        MovieCardDesign(
                            movie = movie, onClick = {
                                navController.navigate(
                                    MovieIdModel(
                                        id = movie.id
                                    )
                                )
                            })
                    }
                }
            }

            is ApiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Something went wrong!")

                }
            }
        }


    }


}


@Composable
private fun MovieCardDesign(movie: Movie, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)).background(LIGHT_GREY).padding(16.dp)
    ) {

        Image(
            painter = rememberImagePainter(movie.poster),
            contentDescription = "poster",
            modifier = Modifier.height(150.dp).width(100.dp).clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                movie.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                "Release Year: ${movie.year}",
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )


            Text(
                "IMDB ID: ${movie.id}", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)
            )


            Button(onClick = {
                onClick()

            }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Play Now")
            }


        }


    }
}