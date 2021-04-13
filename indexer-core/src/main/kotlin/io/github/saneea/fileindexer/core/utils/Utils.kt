package io.github.saneea.fileindexer.core.utils

fun <K, V> MutableMap<K, V>.removeIf(predicate: (key: K) -> Boolean) {
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(next.key)) {
            iterator.remove()
        }
    }
}
