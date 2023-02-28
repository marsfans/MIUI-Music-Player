package kg.erjan.musicplayer.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import kg.erjan.musicplayer.utils.MusicObserverUnsubscribeFn
import kg.erjan.musicplayer.utils.MusicObserver

@Composable
fun <T> EventerEffect(musicObserver: MusicObserver<T>, onEvent: (T) -> Unit) {
    var unsubscribe: MusicObserverUnsubscribeFn? = remember { null }

    LaunchedEffect(LocalLifecycleOwner.current) {
        unsubscribe = musicObserver.subscribe {
            onEvent(it)
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose { unsubscribe?.invoke() }
    }
}