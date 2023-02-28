package kg.erjan.musicplayer.services.music

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kg.erjan.domain.entities.tracks.Tracks
import kg.erjan.musicplayer.utils.MusicUtil
import java.util.*
class MusicService : Service() {

    private var player: MusicPlayer = MusicPlayer(this)
    private val musicBind: IBinder = MusicBinder(this)
    private var position = -1
    private var originalPlayerQueue = mutableListOf<Tracks>()
    private var playingQueue: ArrayList<Tracks> = ArrayList<Tracks>()

    val isPlaying: Boolean get() = player.isPlaying
    val currentSong: Tracks get() = getSongAt(position)
    val onUpdate = player.onUpdate

    val currentPlaybackState: PlaybackState?
        get() = player.currentPlaybackState

    private fun getSongAt(position: Int): Tracks {
        return playingQueue[position]
    }

    fun openQueue(playingQueue: List<Tracks>?, startPosition: Int, startingPlaying: Boolean) {
        if (playingQueue != null && playingQueue.isNotEmpty() && startPosition >= 0 && startPosition < playingQueue.size
        ) {
            originalPlayerQueue = ArrayList(playingQueue)
            this.playingQueue = ArrayList(originalPlayerQueue)
            if (startingPlaying) {
                playSongAt(startPosition)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        return musicBind
    }

    fun play() {
        if (!player.isPlaying) {
            if (!player.isInitialized) {
                playSongAt(position)
            } else {
                player.start()
            }
        }
    }

    fun playNextSong() {
        playSongAt(position + 1)
    }

    fun playPreviousSong() {
        playSongAt(position - 1)
    }

    private fun playSongAt(position: Int) {
        if (openTrackAndPrepareNextAt(position)) {
            play()
        }
    }

    fun pause() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    private fun openTrackAndPrepareNextAt(position: Int): Boolean {
        this.position = position
        val prepared = openCurrent()
        if (prepared) {
            prepareNextImpl()
        }
        return prepared
    }

    private fun prepareNextImpl(): Boolean {
        return true
    }

    private fun openCurrent(): Boolean = synchronized(this) {
        return player.setDataSource(
            getTrackUri(
                Objects.requireNonNull(
                    currentSong
                )
            )
        )
    }

    private fun getTrackUri(tracks: Tracks): String {
        return MusicUtil().getSongFileUri(tracks.id).toString()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    class MusicBinder(val service: MusicService) : Binder()
}