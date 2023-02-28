package kg.erjan.musicplayer.utils

typealias MusicObserverSubscriber<T> = (T) -> Unit
typealias MusicObserverUnsubscribeFn = () -> Unit

class MusicObserver<T> {
    private val subscribers = mutableListOf<MusicObserverSubscriber<T>>()

    fun subscribe(subscriber: MusicObserverSubscriber<T>): MusicObserverUnsubscribeFn {
        subscribers.add(subscriber)
        return { unsubscribe(subscriber) }
    }

    fun unsubscribe(subscriber: MusicObserverSubscriber<T>) {
        subscribers.remove(subscriber)
    }

    fun dispatch(event: T) {
        subscribers.forEach { it(event) }
    }
}