package kg.erjan.musicplayer.services.music

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class MusicPlayer(private val context: Context) {

    private var mIsInitialized = false

    val currentPlaybackState: PlaybackState?
        get() = mediaPlayer?.let {
            PlaybackState(
                played = it.currentPosition,
                total = it.duration
            )
        }

    private val unsafeMediaPlayer: MediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
            mIsInitialized = true
        }
        setOnCompletionListener {
            mIsInitialized = false
        }
        setOnErrorListener { _, _, _ ->
            true
        }

    }
    private val mediaPlayer: MediaPlayer?
        get() = if (mIsInitialized) unsafeMediaPlayer else null

    fun isInitialized(): Boolean = mIsInitialized

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    fun setDataSource(path: String): Boolean {
        mIsInitialized = false
        mIsInitialized = setDataSourceImpl(unsafeMediaPlayer, path)
        return mIsInitialized
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

    fun startMusic(): Boolean {
        return try {
            mediaPlayer?.start()
            true
        } catch (e: java.lang.IllegalStateException) {
            false
        }
    }

    fun pause(): Boolean {
        return try {
            mediaPlayer?.pause()
            true
        } catch (e: java.lang.IllegalStateException) {
            false
        }
    }
}

data class PlaybackState(
    val played: Int,
    val total: Int,
) {

    companion object {
        val zero = PlaybackState(0, 0)
    }
}