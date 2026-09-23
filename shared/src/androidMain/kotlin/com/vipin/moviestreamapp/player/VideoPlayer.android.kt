package com.vipin.moviestreamapp.player


import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView


@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    videoUrl: String,
    adVideoUrl: String,
    adPositionSeconds: Long,
    modifier: Modifier,
    autoPlay: Boolean
) {

    val context = LocalContext.current


    val sessionToken = remember {
        SessionToken(
            context, ComponentName(
                context, PlaybackService::class.java
            )
        )
    }

    val controllerFuture = remember {
        MediaController.Builder(
            context, sessionToken
        ).buildAsync()
    }


    var playbackState by remember(videoUrl) {
        mutableStateOf(
            VideoPlaybackState.INITIAL_LOADING
        )
    }

    // check advertising is play
    var isAdPlaying by remember(videoUrl) {
        mutableStateOf(false)
    }


    var adPlayed by remember(videoUrl) {
        mutableStateOf(false)
    }


    var moviePositionBeforeAd by remember(videoUrl) {
        mutableLongStateOf(0L)
    }


    // Exo Player

    val exoPlayerState = produceState<MediaController?>(
        initialValue = null, key1 = controllerFuture
    ) {
        controllerFuture.addListener(
            {
                try {
                    value = controllerFuture.get()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context)
        )
    }

    if (exoPlayerState.value == null) {
        Box(
            modifier = modifier, contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val exoPlayer = exoPlayerState.value!!



    LaunchedEffect(exoPlayer, videoUrl) {

        val currentUrl = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()

        // Same video is already loaded.
        if (currentUrl == videoUrl) {

            if (autoPlay && !exoPlayer.isPlaying) {
                exoPlayer.play()
            }

            return@LaunchedEffect
        }

        // Different video play (ad play)
        exoPlayer.setMediaItem(
            MediaItem.fromUri(videoUrl)
        )

        exoPlayer.prepare()

        if (autoPlay) {
            exoPlayer.play()
        }
    }


    // Player listener

    DisposableEffect(exoPlayer) {

        val listener = object : Player.Listener {


            // Player Events
            override fun onEvents(
                player: Player, events: Player.Events
            ) {

                // Convert Second to Millis
                val adPositionMs = adPositionSeconds * 1000L


                if (!isAdPlaying && !adPlayed && player.currentMediaItemIndex == 0 && player.currentPosition >= adPositionMs) {


                    moviePositionBeforeAd = player.currentPosition


                    adPlayed = true


                    startAdvertisement(
                        player = player,
                        adUrl = adVideoUrl,
                        moviePosition = moviePositionBeforeAd,
                        onAdStarted = {
                            isAdPlaying = true
                        })
                }
            }


            override fun onPlaybackStateChanged(
                state: Int
            ) {


                if (state == Player.STATE_ENDED && isAdPlaying) {

                    resumeMovie(
                        player = exoPlayer,
                        movieUrl = videoUrl,
                        moviePosition = moviePositionBeforeAd,
                        onAdFinished = {
                            isAdPlaying = false
                        })


                    return
                }



                playbackState = when (state) {

                    Player.STATE_BUFFERING -> {
                        VideoPlaybackState.BUFFERING
                    }

                    Player.STATE_READY -> {

                        if (exoPlayer.isPlaying) {
                            VideoPlaybackState.PLAYING
                        } else {
                            VideoPlaybackState.PAUSED
                        }
                    }

                    Player.STATE_ENDED -> {
                        VideoPlaybackState.ENDED
                    }

                    Player.STATE_IDLE -> {
                        VideoPlaybackState.INITIAL_LOADING
                    }

                    else -> {
                        VideoPlaybackState.INITIAL_LOADING
                    }
                }
            }


            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {


                if (isAdPlaying) {
                    return
                }

                if (exoPlayer.playbackState == Player.STATE_READY) {

                    playbackState = if (isPlaying) {
                        VideoPlaybackState.PLAYING
                    } else {
                        VideoPlaybackState.PAUSED
                    }
                }
            }


            override fun onPlayerError(
                error: PlaybackException
            ) {

                playbackState = VideoPlaybackState.ERROR
            }
        }



        exoPlayer.addListener(listener)



        onDispose {

            exoPlayer.removeListener(listener)
            MediaController.releaseFuture(controllerFuture)


        }
    }




    Box(
        modifier = modifier
    ) {


        AndroidView(
            modifier = Modifier.fillMaxSize(),

            factory = { context ->

                PlayerView(context).apply {

                    player = exoPlayer

                    /*
                     * Show play/pause/seek controls.
                     */
                    useController = true

                    controllerAutoShow = true
                }
            },

            update = { playerView ->

                playerView.player = exoPlayer
            })




        if (isAdPlaying) {

            Text(
                text = "ADVERTISEMENT",

                modifier = Modifier.align(Alignment.TopStart).background(
                        Color.Black.copy(
                            alpha = 0.75f
                        )
                    ),

                color = Color.White
            )
        }



        if (playbackState == VideoPlaybackState.INITIAL_LOADING) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }

        // Show loading when Video buffer

        if (playbackState == VideoPlaybackState.BUFFERING) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }


        //  Error
        if (playbackState == VideoPlaybackState.ERROR) {

            Text(
                text = "Unable to play video",

                modifier = Modifier.align(Alignment.Center),

                color = Color.White
            )
        }
    }
}


// Start Advertisement

private fun startAdvertisement(
    player: Player, adUrl: String, moviePosition: Long, onAdStarted: () -> Unit
) {

    // Start Ad
    onAdStarted()


    // Pause Video
    player.pause()


    // change video with ads
    player.setMediaItem(
        MediaItem.fromUri(adUrl)
    )


    player.prepare()



    player.play()
}


//  resume video

private fun resumeMovie(
    player: Player, movieUrl: String, moviePosition: Long, onAdFinished: () -> Unit
) {


    onAdFinished()



    player.setMediaItem(
        MediaItem.fromUri(movieUrl)
    )


    player.prepare()



    player.seekTo(
        moviePosition
    )


    player.play()
}


