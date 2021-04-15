package io.github.saneea.fileindexer.core.utils

class Cache<K : Any, T : Any> {

    private data class CachedEntry<T>(val cachedObject: T, var counter: Int = 0)

    private val cachedEntries = HashMap<K, CachedEntry<T>>()

    fun getOrCreate(key: K, factory: (K) -> T): T {
        var cachedEntry = cachedEntries[key]
        if (cachedEntry == null) {
            cachedEntry = CachedEntry(factory(key))
            cachedEntries[key] = cachedEntry
        }
        cachedEntry.counter++
        return cachedEntry.cachedObject
    }

    fun free(key: K, destroyAction: (K, T) -> Unit) {
        val cachedEntry = cachedEntries[key]
        if (cachedEntry != null && with(cachedEntry) { --counter } == 0) {
            cachedEntries.remove(key)
            destroyAction(key, cachedEntry.cachedObject)
        }
    }

}
