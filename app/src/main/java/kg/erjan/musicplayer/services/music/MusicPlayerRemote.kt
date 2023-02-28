package kg.erjan.musicplayer.services.music

import android.app.Activity
import android.content.*
import android.os.IBinder
import androidx.core.content.ContextCompat
import kg.erjan.domain.entities.tracks.Tracks
import kg.erjan.musicplayer.utils.MusicObserver
import java.util.*

class MusicPlayerRemote {

    private val connectionMap = WeakHashMap<Context, ServiceBinder>()
    var musicService = MusicService()
    val currentSong: Tracks = musicService.currentSong
    val onUpdate: MusicObserver<MusicState> = musicService.onUpdate
    val isPlaying: Boolean = musicService.isPlaying

    val currentPlaybackState: PlaybackState? = musicService.currentPlaybackState

    fun playNextSong() {
        musicService.playNextSong()
    }

    fun playPreviousSong() {
        musicService.playPreviousSong()
    }

    fun pauseSong() {
        musicService.pause()
    }

    fun resumePlaying() {
        musicService.play()
    }

    fun openQueue(queue: List<Tracks>, startPosition: Int, startPlaying: Boolean) {
        musicService.openQueue(queue, startPosition, startPlaying)
    }

    fun bindService(context: Context, callback: ServiceConnection): ServiceToken? {
        var activity: Activity? = (context as Activity).parent
        if (activity == null) {
            activity = context
        }

        val contextWrapper = ContextWrapper(activity)
        val intent = Intent(contextWrapper, MusicService::class.java)
        try {
            contextWrapper.startService(intent)
        } catch (ignored: IllegalStateException) {
            ContextCompat.startForegroundService(context, intent)
        }

        val binder = ServiceBinder(callback)
        if (contextWrapper.bindService(
                Intent().setClass(context, MusicService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            connectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = connectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
    }

    inner class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}