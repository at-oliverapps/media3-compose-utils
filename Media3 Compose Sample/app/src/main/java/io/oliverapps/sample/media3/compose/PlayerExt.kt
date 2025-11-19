package io.oliverapps.sample.media3.compose

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * A helper extension to easily get a List<MediaItem> from the Player.
 * Place this in a 'PlayerExtensions.kt' file.
 *
 * You shouldn't ideally use this method as you prob get your data from a database
 * and manage your list by yourself and for my use this method seems unstable
 * but its good if you just want your app up and running
 */
val Player.mediaItems: List<MediaItem>
    get() = object : AbstractList<MediaItem>() {
        override val size: Int
            get() = mediaItemCount
        override fun get(index: Int): MediaItem = getMediaItemAt(index)
    }

