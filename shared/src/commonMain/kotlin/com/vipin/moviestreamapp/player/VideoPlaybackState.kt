package com.vipin.moviestreamapp.player

enum class VideoPlaybackState {
    INITIAL_LOADING,
    PLAYING,
    PAUSED,
    BUFFERING,
    ENDED,
    ERROR
}