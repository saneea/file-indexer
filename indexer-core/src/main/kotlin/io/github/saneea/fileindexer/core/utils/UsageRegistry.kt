package io.github.saneea.fileindexer.core.utils

class UsageRegistry<T> {

    private val counters = HashMap<T, Counter>()

    private data class Counter(var current: Int = 0)

    fun inUse(obj: T) {
        getCounter(obj).current++
    }

    fun free(obj: T, destructor: (T) -> Unit) {
        if (with(getCounter(obj)) { --current } == 0) {
            counters.remove(obj)
            destructor(obj)
        }
    }

    private fun getCounter(obj: T): Counter =
        counters[obj] ?: Counter().apply { counters[obj] = this }

}