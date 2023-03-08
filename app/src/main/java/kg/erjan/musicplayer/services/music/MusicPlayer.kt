package kg.erjan.musicplayer.services.music

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.util.*

class MusicPlayer(private val context: Context) {

    var isInitialized = false
    var onPlaybackPositionUpdate: ((PlaybackPosition) -> Unit)? = null
    private var playbackPositionUpdate: Timer? = null

    val currentPlaybackPosition: PlaybackPosition?
        get() = mediaPlayer?.let {
            PlaybackPosition(
                played = it.currentPosition,
                total = it.duration
            )
        }

    private val unsafeMediaPlayer: MediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
            setDurationToPlaybackPosition()
            isInitialized = true
        }
        setOnCompletionListener {
            isInitialized = false
        }
        setOnErrorListener { _, _, _ ->
            true
        }

    }
    private val mediaPlayer: MediaPlayer?
        get() = if (isInitialized) unsafeMediaPlayer else null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    fun setDataSource(path: String): Boolean {
        isInitialized = false
        isInitialized = setDataSourceImpl(unsafeMediaPlayer, path)
        return isInitialized
    }

    private fun setDataSourceImpl(player: MediaPlayer, path: String): Boolean {
        try {
            player.reset()
            player.setOnPreparedListener(null)
            if (path.startsWith("content://")) {
                player.setDataSource(context, Uri.parse(path))
            } else {
                player.setDataSource(path)
            }
            player.prepare()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun start() = mediaPlayer?.start()

    fun pause() = mediaPlayer?.pause()

    private fun setDurationToPlaybackPosition() {
        playbackPositionUpdate = kotlin.concurrent.timer(period = 100L) {
            currentPlaybackPosition?.let {
                onPlaybackPositionUpdate?.invoke(it)
            }
        }
    }

}

data class PlaybackPosition(
    val played: Int,
    val total: Int,
) {

    companion object {
        val zero = PlaybackPosition(0, 0)
    }
}