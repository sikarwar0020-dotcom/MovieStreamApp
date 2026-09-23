package com.vipin.moviestreamapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String,
    val title: String,
    val year: String,
    val poster: String,
    val url: String
)