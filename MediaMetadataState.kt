package io.oliverapps.radio.player.state

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.media3.common.Player
import androidx.media3.common.Rating
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import com.google.common.collect.ImmutableList

@UnstableApi
@Composable
fun rememberMediaMetadata(player: Player): MediaMetadataState {
    // Remember a new MetadataState instance whenever the player instance changes.
    val metadataState = remember(player) { MediaMetadataState(player) }

    // Launch a coroutine to observe media metadata changes whenever the player instance changes.
    LaunchedEffect(player) {
        metadataState.observe()
    }
    return metadataState
}

@UnstableApi
class MediaMetadataState(private val player: Player) {

    // --- Individual MediaMetadata Fields ---
    /** Optional title. */
    var title: CharSequence? by mutableStateOf(null)
        private set

    /** Optional artist. */
    var artist: CharSequence? by mutableStateOf(null)
        private set

    /** Optional album title. */
    var albumTitle: CharSequence? by mutableStateOf(null)
        private set

    /** Optional album artist. */
    var albumArtist: CharSequence? by mutableStateOf(null)
        private set

    /** Optional display title. */
    var displayTitle: CharSequence? by mutableStateOf(null)
        private set

    /**
     * Optional subtitle.
     *
     * This is the secondary title of the media, unrelated to closed captions.
     */
    var subtitle: CharSequence? by mutableStateOf(null)
        private set

    /** Optional description. */
    var description: CharSequence? by mutableStateOf(null)
        private set

    /**
     * Optional duration, non-negative and in milliseconds.
     *
     * This field is populated by the app when building the metadata object and is for
     * informational purpose only. For retrieving the duration of the media item currently being
     * played, use [Player.getDuration] instead.
     */
    var durationMs: Long? by mutableStateOf(null)
        private set

    /** Optional user [Rating]. */
    var userRating: Rating? by mutableStateOf(null)
        private set

    /** Optional overall [Rating]. */
    var overallRating: Rating? by mutableStateOf(null)
        private set

    /** Optional artwork data as a compressed byte array. */
    var artworkData: ByteArray? by mutableStateOf(null)
        private set

    /** Optional [PictureType] of the artwork data. */
    var artworkDataType: Int? by mutableStateOf(null) // Integer in Java becomes Int in Kotlin
        private set

    /** Optional artwork [Uri]. */
    var artworkUri: Uri? by mutableStateOf(null)
        private set

    /** Optional track number. */
    var trackNumber: Int? by mutableStateOf(null)
        private set

    /** Optional total number of tracks. */
    var totalTrackCount: Int? by mutableStateOf(null)

    /**
     * Optional [FolderType].
     *
     * @deprecated Use [isBrowsable] to indicate if an item is a browsable folder and use
     * [mediaType] to indicate the type of the folder.
     */
    @Deprecated(
        "Use isBrowsable to indicate if an item is a browsable folder and use mediaType to indicate the type of the folder."
    )
    @Suppress("deprecation") // Defining field of deprecated type.
    var folderType: Int? by mutableStateOf(null)
        private set

    /** Optional boolean to indicate that the media is a browsable folder. */
    var isBrowsable: Boolean? by mutableStateOf(null)
        private set

    /** Optional boolean to indicate that the media is playable. */
    var isPlayable: Boolean? by mutableStateOf(null)
        private set

    /**
     * @deprecated Use [recordingYear] instead.
     */
    @Deprecated("Use recordingYear instead.")
    var year: Int? by mutableStateOf(null)
        private set

    /** Optional year of the recording date. */
    var recordingYear: Int? by mutableStateOf(null)
        private set

    /**
     * Optional month of the recording date.
     *
     * <p>Note that there is no guarantee that the month and day are a valid combination.
     */
    var recordingMonth: Int? by mutableStateOf(null)
        private set

    /**
     * Optional day of the recording date.
     *
     * <p>Note that there is no guarantee that the month and day are a valid combination.
     */
    var recordingDay: Int? by mutableStateOf(null)
        private set

    /** Optional year of the release date. */
    var releaseYear: Int? by mutableStateOf(null)
        private set

    /**
     * Optional month of the release date.
     *
     * <p>Note that there is no guarantee that the month and day are a valid combination.
     */
    var releaseMonth: Int? by mutableStateOf(null)
        private set

    /**
     * Optional day of the release date.
     *
     * <p>Note that there is no guarantee that the month and day are a valid combination.
     */
    var releaseDay: Int? by mutableStateOf(null)
        private set

    /** Optional writer. */
    var writer: CharSequence? by mutableStateOf(null)
        private set

    /** Optional composer. */
    var composer: CharSequence? by mutableStateOf(null)
        private set

    /** Optional conductor. */
    var conductor: CharSequence? by mutableStateOf(null)
        private set

    /** Optional disc number. */
    var discNumber: Int? by mutableStateOf(null)
        private set

    /** Optional total number of discs. */
    var totalDiscCount: Int? by mutableStateOf(null)
        private set

    /** Optional genre. */
    var genre: CharSequence? by mutableStateOf(null)
        private set

    /** Optional compilation. */
    var compilation: CharSequence? by mutableStateOf(null)
        private set

    /** Optional name of the station streaming the media. */
    var station: CharSequence? by mutableStateOf(null)
        private set

    /** Optional [MediaType]. */
    var mediaType: Int? by mutableStateOf(null)
        private set

    /**
     * Optional extras [Bundle].
     *
     * Given the complexities of checking the equality of two [Bundle] instances, the
     * contents of these extras are not considered in the [equals] and [hashCode] implementation.
     */
    var extras: Bundle? by mutableStateOf(null)
        private set

    /**
     * The IDs of the supported commands of this media item (see for instance `CommandButton.sessionCommand.customAction`
     * of the Media3 session module).
     */
    var supportedCommands: ImmutableList<String> by mutableStateOf(ImmutableList.of())
        private set


    /**
     * Subscribes to updates from [Player.Events] and listens to
     * [Player.EVENT_MEDIA_METADATA_CHANGED] in order to update the [metadata] state.
     * This function should be called within a CoroutineScope, typically from a [LaunchedEffect].
     */
    suspend fun observe(): Nothing {
        // Immediately update the state with the current media metadata when observation starts.
        // This handles cases where media metadata might be available before the first event.
        title = player.mediaMetadata.title
        artist = player.mediaMetadata.artist
        albumTitle = player.mediaMetadata.albumTitle
        albumArtist = player.mediaMetadata.albumArtist
        displayTitle = player.mediaMetadata.displayTitle
        subtitle = player.mediaMetadata.subtitle
        description = player.mediaMetadata.description
        durationMs = player.mediaMetadata.durationMs
        userRating = player.mediaMetadata.userRating
        overallRating = player.mediaMetadata.overallRating
        artworkData = player.mediaMetadata.artworkData
        artworkDataType = player.mediaMetadata.artworkDataType
        artworkUri = player.mediaMetadata.artworkUri
        trackNumber = player.mediaMetadata.trackNumber
        totalTrackCount = player.mediaMetadata.totalTrackCount
        @Suppress("deprecation") // Assigning deprecated field
        folderType = player.mediaMetadata.folderType
        isBrowsable = player.mediaMetadata.isBrowsable
        isPlayable = player.mediaMetadata.isPlayable
        @Suppress("deprecation") // Assigning deprecated field
        year = player.mediaMetadata.year
        recordingYear = player.mediaMetadata.recordingYear
        recordingMonth = player.mediaMetadata.recordingMonth
        recordingDay = player.mediaMetadata.recordingDay
        releaseYear = player.mediaMetadata.releaseYear
        releaseMonth = player.mediaMetadata.releaseMonth
        releaseDay = player.mediaMetadata.releaseDay
        writer = player.mediaMetadata.writer
        composer = player.mediaMetadata.composer
        conductor = player.mediaMetadata.conductor
        discNumber = player.mediaMetadata.discNumber
        totalDiscCount = player.mediaMetadata.totalDiscCount
        genre = player.mediaMetadata.genre
        compilation = player.mediaMetadata.compilation
        station = player.mediaMetadata.station
        mediaType = player.mediaMetadata.mediaType
        extras = player.mediaMetadata.extras
        supportedCommands = player.mediaMetadata.supportedCommands

        // Listen for player events. The 'listen' function is assumed to be provided by your Player API.
        // It takes a lambda that receives the set of events that occurred.
        player.listen { events ->
            // Check if the media metadata changed event is present in the received events.
            if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)) { // Changed to EVENT_MEDIA_METADATA_CHANGED
                // If media metadata changed, update the 'metadata' state with the player's new media metadata.
                title = player.mediaMetadata.title
                artist = player.mediaMetadata.artist
                albumTitle = player.mediaMetadata.albumTitle
                albumArtist = player.mediaMetadata.albumArtist
                displayTitle = player.mediaMetadata.displayTitle
                subtitle = player.mediaMetadata.subtitle
                description = player.mediaMetadata.description
                durationMs = player.mediaMetadata.durationMs
                userRating = player.mediaMetadata.userRating
                overallRating = player.mediaMetadata.overallRating
                artworkData = player.mediaMetadata.artworkData
                artworkDataType = player.mediaMetadata.artworkDataType
                artworkUri = player.mediaMetadata.artworkUri
                trackNumber = player.mediaMetadata.trackNumber
                totalTrackCount = player.mediaMetadata.totalTrackCount
                @Suppress("deprecation") // Assigning deprecated field
                folderType = player.mediaMetadata.folderType
                isBrowsable = player.mediaMetadata.isBrowsable
                isPlayable = player.mediaMetadata.isPlayable
                @Suppress("deprecation") // Assigning deprecated field
                year = player.mediaMetadata.year
                recordingYear = player.mediaMetadata.recordingYear
                recordingMonth = player.mediaMetadata.recordingMonth
                recordingDay = player.mediaMetadata.recordingDay
                releaseYear = player.mediaMetadata.releaseYear
                releaseMonth = player.mediaMetadata.releaseMonth
                releaseDay = player.mediaMetadata.releaseDay
                writer = player.mediaMetadata.writer
                composer = player.mediaMetadata.composer
                conductor = player.mediaMetadata.conductor
                discNumber = player.mediaMetadata.discNumber
                totalDiscCount = player.mediaMetadata.totalDiscCount
                genre = player.mediaMetadata.genre
                compilation = player.mediaMetadata.compilation
                station = player.mediaMetadata.station
                mediaType = player.mediaMetadata.mediaType
                extras = player.mediaMetadata.extras
                supportedCommands = player.mediaMetadata.supportedCommands
            }
        }
        // The 'observe' function is designed to run indefinitely, similar to a stream listener.
        // It will keep the coroutine active as long as the player is being observed.
        // If the player.listen function doesn't complete (e.g., it's a suspending listen),
        // then Nothing means this function will not return normally.
    }
}