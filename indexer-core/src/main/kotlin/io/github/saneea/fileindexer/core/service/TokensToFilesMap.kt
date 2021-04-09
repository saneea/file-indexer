package io.github.saneea.fileindexer.core.service

import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class TokensToFilesMap {
    private val tokensToFiles: ConcurrentMap<String, ConcurrentMap<Path, Any>> = ConcurrentHashMap()

    override fun toString() = tokensToFiles.toString()

    fun addFileForToken(token: String, filePath: Path) {
        getFilesSetForToken(token)[filePath] = Any()
    }

    fun removeFile(filePath: Path) {
        tokensToFiles.forEach { (_, fileSet) ->
            fileSet.remove(filePath)
        }
    }

    fun getFilesForToken(token: String): Set<Path> =
        tokensToFiles[token]?.keys ?: Collections.emptySet()

    private fun getFilesSetForToken(token: String): ConcurrentMap<Path, Any> =
        tokensToFiles.computeIfAbsent(token) { ConcurrentHashMap() }
}