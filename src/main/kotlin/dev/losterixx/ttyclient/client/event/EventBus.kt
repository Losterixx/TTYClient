package dev.losterixx.ttyclient.client.event

object EventBus {

    @PublishedApi
    internal val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> on(noinline handler: (T) -> Unit) {
        listeners.getOrPut(T::class.java) { mutableListOf() }.add(handler as (Any) -> Unit)
    }

    fun <T : Any> post(event: T) {
        listeners[event::class.java]?.toList()?.forEach { it(event) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> unsubscribe(type: Class<T>, handler: (T) -> Unit) {
        listeners[type]?.remove(handler as (Any) -> Unit)
    }

    fun clear() {
        listeners.clear()
    }
}


