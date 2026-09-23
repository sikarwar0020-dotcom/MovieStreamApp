package com.vipin.moviestreamapp.player

import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeMoviePlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    videoUrl: String,
    adVideoUrl: String,
    adPositionSeconds: Long,
    modifier: Modifier,
    autoPlay: Boolean
) {


    var isAdPlaying by remember(videoUrl) {
        mutableStateOf(false)
    }

    var adPlayed by remember(videoUrl) {
        mutableStateOf(false)
    }


    var moviePositionBeforeAd by remember(videoUrl) {
        mutableLongStateOf(0L)
    }

    var playbackState by remember(videoUrl) {
        mutableStateOf(
            VideoPlaybackState.INITIAL_LOADING
        )
    }





    if (videoUrl.isNullOrEmpty()) {
        playbackState = VideoPlaybackState.ERROR
        return
    }

    val player = remember(videoUrl) {

        AVPlayer(
            uRL = NSURL.URLWithString(videoUrl)!!
        )
    }


    val playerViewController = remember(player) {

        AVPlayerViewController().apply {

            this.player = player

            showsPlaybackControls = true
            allowsPictureInPicturePlayback = true
            canStartPictureInPictureAutomaticallyFromInline = true
        }
    }



    DisposableEffect(
        player, adVideoUrl, adPositionSeconds
    ) {


        configureAudioSession()

        if (autoPlay) {
            player.play()
        }


        val timeObserver = player.addPeriodicTimeObserverForInterval(
            interval = CMTimeMakeWithSeconds(
                seconds = 0.5, preferredTimescale = 600
            ), queue = null
        ) { time ->

            when (player.timeControlStatus) {

                AVPlayerTimeControlStatusPlaying -> {
                    playbackState = VideoPlaybackState.PLAYING
                }

                AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate -> {
                    playbackState = VideoPlaybackState.BUFFERING
                }

                AVPlayerTimeControlStatusPaused -> {
                    playbackState = VideoPlaybackState.PAUSED
                }
            }




            if (isAdPlaying) {
                return@addPeriodicTimeObserverForInterval
            }


            if (adPlayed) {
                return@addPeriodicTimeObserverForInterval
            }

            val currentSeconds = CMTimeGetSeconds(time)


            if (currentSeconds.isNaN() || currentSeconds.isInfinite()) {
                return@addPeriodicTimeObserverForInterval
            }


            if (currentSeconds >= adPositionSeconds.toDouble()) {


                moviePositionBeforeAd = (currentSeconds * 1000L).toLong()


                adPlayed = true

                // Start Advertisement
                startAdvertisement(
                    player = player, adUrl = adVideoUrl, onAdStarted = {
                        isAdPlaying = true
                    })
            }
        }




        val endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AVPlayerItemDidPlayToEndTimeNotification", `object` = null, queue = null
        ) {

            // ad finish, play new video
            if (isAdPlaying) {

                resumeMovie(
                    player = player,
                    movieUrl = videoUrl,
                    moviePosition = moviePositionBeforeAd,
                    onAdFinished = {
                        isAdPlaying = false
                    })

            } else {


                playbackState = VideoPlaybackState.ENDED
            }
        }




        val interruptionObserver: NSObjectProtocol =
            NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVAudioSessionInterruptionNotification, `object` = null, queue = null
            ) { notification ->

                val userInfo = notification?.userInfo
                val typeValue =
                    (userInfo?.get(AVAudioSessionInterruptionTypeKey) as? NSNumber)?.unsignedLongValue

                when (typeValue) {

                    AVAudioSessionInterruptionTypeBegan -> {

                        playbackState = VideoPlaybackState.PAUSED
                    }

                    AVAudioSessionInterruptionTypeEnded -> {

                        val optionsValue =
                            (userInfo?.get(AVAudioSessionInterruptionOptionKey) as? NSNumber)?.unsignedLongValue
                                ?: 0uL

                        val shouldResume =
                            (optionsValue and AVAudioSessionInterruptionOptionShouldResume) != 0uL


                        configureAudioSession()

                        if (shouldResume || autoPlay) {
                            player.play()
                        }
                    }
                }
            }




        onDispose {

            player.removeTimeObserver(
                timeObserver
            )

            NSNotificationCenter.defaultCenter.removeObserver(
                endObserver
            )

            NSNotificationCenter.defaultCenter.removeObserver(
                interruptionObserver
            )


            player.replaceCurrentItemWithPlayerItem(null)
        }
    }



    Box(
        modifier = modifier
    ) {

        /*
         * Native AVPlayer UI.
         */
        UIKitView(

            modifier = Modifier.fillMaxSize(),

            factory = {
                playerViewController.view
            },

            update = {
                playerViewController.player = player
            })


        /*
         * -----------------------------------------------------
         * ADVERTISEMENT LABEL
         * -----------------------------------------------------
         */

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


        /*
         * -----------------------------------------------------
         * LOADING
         * -----------------------------------------------------
         */

        if (playbackState == VideoPlaybackState.INITIAL_LOADING) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }


        /*
         * -----------------------------------------------------
         * BUFFERING
         * -----------------------------------------------------
         */

        if (playbackState == VideoPlaybackState.BUFFERING) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }


        /*
         * -----------------------------------------------------
         * ERROR
         * -----------------------------------------------------
         */

        if (playbackState == VideoPlaybackState.ERROR) {

            Text(
                text = "Unable to play video",

                modifier = Modifier.align(Alignment.Center),

                color = Color.White
            )
        }
    }
}


@OptIn(ExperimentalForeignApi::class)
private fun startAdvertisement(
    player: AVPlayer, adUrl: String, onAdStarted: () -> Unit
) {

    /*
     * Tell UI that advertisement has started.
     */
    onAdStarted()

    /*
     * Pause movie.
     */
    player.pause()

    /*
     * Create advertisement URL.
     */
    val adUrlObject = NSURL.URLWithString(adUrl)

    /*
     * Create advertisement item.
     */
    val adItem = AVPlayerItem(
        uRL = adUrlObject!!
    )

    /*
     * Replace movie with advertisement.
     */
    player.replaceCurrentItemWithPlayerItem(
        adItem
    )

    /*
     * Autoplay advertisement.
     */
    player.play()
}


@OptIn(ExperimentalForeignApi::class)
private fun resumeMovie(
    player: AVPlayer, movieUrl: String, moviePosition: Long, onAdFinished: () -> Unit
) {
    val movieUrlObject = NSURL.URLWithString(movieUrl)

    if (movieUrlObject == null) {
        onAdFinished()
        return
    }

    val movieItem = AVPlayerItem(
        uRL = movieUrlObject
    )

    player.replaceCurrentItemWithPlayerItem(movieItem)

    val positionSeconds = moviePosition.toDouble() / 1000.0

    player.seekToTime(
        CMTimeMakeWithSeconds(
            seconds = positionSeconds, preferredTimescale = 600
        )
    )

    onAdFinished()

    player.play()
}


/*
 * =========================================================================
 * AUDIO SESSION CONFIGURATION
 * =========================================================================
 *
 * category = .playback (not .ambient / .soloAmbient) is what allows audio
 * to keep playing when the app is backgrounded or the screen locks, and
 * even when the Ring/Silent switch is set to silent. This was already
 * correct in the original file — the missing piece for true background
 * playback is the Info.plist "UIBackgroundModes: audio" entry described
 * in the comment block at the top of this file.
 */
@OptIn(ExperimentalForeignApi::class)
private fun configureAudioSession() {
    val audioSession = AVAudioSession.sharedInstance()

    audioSession.setCategory(
        category = AVAudioSessionCategoryPlayback,
        mode = AVAudioSessionModeMoviePlayback,
        options = 0u,
        error = null
    )

    audioSession.setActive(
        active = true, error = null
    )
}