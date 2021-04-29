package io.github.saneea.fileindexer.core.common

import io.github.saneea.fileindexer.core.common.FSEventKind.*

enum class FSEventKind {
    CREATE, DELETE, MODIFY
}

fun List<FSEventKind>.merge() = when (this.size) {
    0 -> null
    1 -> this.first()
    else -> this.first() merge this.last()
}

infix fun FSEventKind.merge(last: FSEventKind) = when (this) {
    CREATE -> when (last) {
        CREATE, MODIFY -> CREATE
        DELETE -> null
    }
    MODIFY, DELETE -> when (last) {
        CREATE -> MODIFY
        MODIFY, DELETE -> last
    }
}