package io.oliverapps.media3.compose.shared.ui

import android.annotation.SuppressLint
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@SuppressLint("UnsafeOptInUsageError")
class PlaybackService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession

    //used by wearos
    val audioOffloadPreferences = TrackSelectionParameters.AudioOffloadPreferences.Builder()
        .setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
        .build()

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build()
            .apply {

                //this isn't necessary its just there so you can seek through the playing contents of a station faster
                setSeekBackIncrementMs( 30*1000L )
                setSeekForwardIncrementMs( 30*1000L )

                setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
                setHandleAudioBecomingNoisy(true)
                setWakeMode(WAKE_MODE_NETWORK)

                trackSelectionParameters.buildUpon().setAudioOffloadPreferences(audioOffloadPreferences).build()

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
                    MediaItem.Builder().setMediaId("0").setUri("https://play.ilovemusic.de/ilm_ilove2000throwbacks/").setMediaMetadata(MediaMetadata.Builder().setTitle("I LOVE 2000+ THROWBACKS").setArtworkUri("https://radio.oliverapps.io/api/graphics/logo/germany/i_love/2000_plus_throwbacks.png".toUri()).build()).build(),
                    MediaItem.Builder().setMediaId("1").setUri("http://nebula.shoutca.st:8545/stream").setMediaMetadata(MediaMetadata.Builder().setTitle("ZFM").setArtworkUri("https://nz.radio.net/300/zfm.png".toUri()).build()).build(),
                    MediaItem.Builder().setMediaId("2").setUri("https://live.visir.is/hls-radio/fm957/chunklist_DVR.m3u8").setMediaMetadata(MediaMetadata.Builder().setTitle("FM 957").setArtworkUri("https://www.visir.is/mi/300x300/ci/ef50c5c5-6abf-4dfe-910c-04d88b6bdaef.png".toUri()).build()).build()
                )
            )
            prepare()
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