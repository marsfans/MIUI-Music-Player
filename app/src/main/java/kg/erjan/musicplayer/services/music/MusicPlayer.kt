package kg.erjan.musicplayer.services.music

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kg.erjan.musicplayer.utils.MusicObserver

enum class MusicState {
    StartPlaying,
    StopPlaying,
    SongStaged
}
class MusicPlayer(private val context: Context) {

    var isInitialized = false
    val onUpdate = MusicObserver<MusicState>()

    val currentPlaybackState: PlaybackState?
        get() = mediaPlayer?.let {
            PlaybackState(
                played = it.currentPosition,
                total = it.duration
            )
        }

    private val unsafeMediaPlayer: MediaPlayer = MediaPlayer().apply {
        setOnPreparedListener {
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
            onUpdate.dispatch(MusicState.SongStaged)
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

    fun start() {
        mediaPlayer?.start()
        onUpdate.dispatch(MusicState.StartPlaying)
    }

    fun pause() {
        mediaPlayer?.pause()
        onUpdate.dispatch(MusicState.StopPlaying)
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