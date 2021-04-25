package io.github.saneea.fileindexer.core.utils.index

import java.util.*

class IndexTreeNodeBuilder<T : Any, R : Any>(origConstTree: IndexTreeNode<T, R>? = null) {

    var nodeResult: R? = null

    private val children = HashMap<T, ConstOrMutableNode<T, R>>()

    init {
        if (origConstTree != null) {
            nodeResult = origConstTree.result
            for (childId in origConstTree.childIds) {
                children[childId] = ConstOrMutableNode(origConstTree[childId])
            }
        }
    }

    fun removeChild(childId: T) {
        children.remove(childId)
    }

    fun getOrCreateChildBuilder(childId: T): IndexTreeNodeBuilder<T, R> {
        var constOrMutableNode = children[childId]
        if (constOrMutableNode == null) {
            constOrMutableNode = ConstOrMutableNode()
            children[childId] = constOrMutableNode
        }

        var mutable = constOrMutableNode.mutable
        if (mutable == null) {
            mutable = IndexTreeNodeBuilder(constOrMutableNode.const)
            constOrMutableNode.const = null
            constOrMutableNode.mutable = mutable
        }

        return mutable
    }

    fun build(): IndexTreeNode<T, R> =
        IndexTreeNode(
            nodeResult,
            children
                .mapValues { (_, v) -> v.mutable?.build() ?: v.const }
                .filterValues(Objects::nonNull)
                .mapValues { (_, v) -> v!! }
        )
}

private data class ConstOrMutableNode<T : Any, R : Any>(
    var const: IndexTreeNode<T, R>? = null,
    var mutable: IndexTreeNodeBuilder<T, R>? = null
)

fun <T : Any, R : Any> IndexTreeNodeBuilder<T, R>.getOrCreateBranch(sequence: Iterable<T>, result: R) =
    getOrCreateBranch(sequence).also {
        it.nodeResult = result
    }

fun <T : Any, R : Any> IndexTreeNodeBuilder<T, R>.getOrCreateBranch(sequence: Iterable<T>): IndexTreeNodeBuilder<T, R> {
    var currentBuilder = this
    for (sequenceElement in sequence) {
        currentBuilder = currentBuilder.getOrCreateChildBuilder(sequenceElement)
    }
    return currentBuilder
}
