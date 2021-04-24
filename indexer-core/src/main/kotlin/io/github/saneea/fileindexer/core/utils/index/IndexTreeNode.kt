package io.github.saneea.fileindexer.core.utils.index

class IndexTreeNode<T : Any, R : Any>(
    val result: R?,
    private val children: Map<T, IndexTreeNode<T, R>>
) {
    val childIds: Set<T>
        get() = children.keys

    operator fun get(childId: T) = children[childId]

    override fun toString(): String = "data=$result, children=$children"
}