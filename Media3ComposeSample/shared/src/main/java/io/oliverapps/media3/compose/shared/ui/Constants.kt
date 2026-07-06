package io.oliverapps.media3.compose.shared.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.ui.graphics.vector.ImageVector

private val icon = Icons.Rounded
object Constants {

    object Shared {

        object String {
            val Pause = androidx.media3.session.R.string.media3_controls_pause_description
            val Play = androidx.media3.session.R.string.media3_controls_play_description

            val Boolean.PlayPauseContentDescription:  Int
                @StringRes
                get() = if (this) Pause else Play

        }

        object Icon {
            val Equalizer = icon.Equalizer
            val Pause = icon.Pause
            val Play = icon.PlayArrow

            val Boolean.PlayPauseIcon:  ImageVector
                get() = if (this) Pause else Play


        }

    }

    object Mobile {

        object String {
            val Previous = androidx.media3.session.R.string.media3_controls_seek_to_previous_description
            val Next = androidx.media3.session.R.string.media3_controls_seek_to_next_description
            val FastRewind = androidx.media3.session.R.string.media3_controls_seek_back_description
            val FastForward = androidx.media3.session.R.string.media3_controls_seek_forward_description
        }

        object Icon {
            val Next = icon.SkipNext
            val Previous = icon.SkipPrevious
            val FastRewind = icon.FastRewind
            val FastForward = icon.FastForward
        }

    }

}