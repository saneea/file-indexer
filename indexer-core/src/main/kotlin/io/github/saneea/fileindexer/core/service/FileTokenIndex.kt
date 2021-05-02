package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.utils.index.IndexTreeNode
import java.nio.file.Path

typealias FileTokenIndex = IndexTreeNode<Char, Set<Path>>