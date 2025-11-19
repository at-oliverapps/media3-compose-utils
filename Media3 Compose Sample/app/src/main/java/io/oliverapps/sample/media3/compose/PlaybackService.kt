package io.oliverapps.sample.media3.compose

import android.annotation.SuppressLint
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@SuppressLint("UnsafeOptInUsageError")
class PlaybackService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build()
            .apply {

                //this isn't necessary its just there so you can seek through the playing contents of a station faster
                setSeekBackIncrementMs( 30*1000L )
                setSeekForwardIncrementMs( 30*1000L )

                setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
                setHandleAudioBecomingNoisy(true)
                setWakeMode(WAKE_MODE_NETWORK)

            }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, player).build()

        player.run {
            addMediaItems(
                listOf(
                    MediaItem.Builder().setMediaId("1").setUri("http://nebula.shoutca.st:8545/stream").setMediaMetadata(MediaMetadata.Builder().setStation("ZFM").setArtworkUri("https://nz.radio.net/300/zfm.png?version=a00d95bdda87861f1584dc30caffb0f9".toUri()).build()).build(),
                    MediaItem.Builder().setMediaId("2").setUri("https://live.visir.is/hls-radio/fm957/chunklist_DVR.m3u8").setMediaMetadata(MediaMetadata.Builder().setStation("FM 957").setArtworkUri("https://www.visir.is/mi/300x300/ci/ef50c5c5-6abf-4dfe-910c-04d88b6bdaef.png".toUri()).build()).build()
                )
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            release()
            player.stop()
            player.release()
        }
    }

}