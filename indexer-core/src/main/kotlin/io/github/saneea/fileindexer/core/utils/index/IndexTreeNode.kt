package io.github.saneea.fileindexer.core.utils.index

fun <T : Any, R : Any> emptyIndexTree(): IndexTreeNode<T, R> = IndexTreeNode(null, HashMap())

class IndexTreeNode<T : Any, R : Any>(
    val result: R?,
    internal val children: Map<T, IndexTreeNode<T, R>>
) {
    val childIds: Set<T>
        get() = children.keys

    operator fun get(childId: T) = children[childId]

    override fun toString(): String = "data=$result, children=$children"
}

fun <T : Any, R : Any> IndexTreeNode<T, R>.selectResult(path: Iterable<T>): R? = selectNode(path)?.result

fun <T : Any, R : Any> IndexTreeNode<T, R>.selectNode(path: Iterable<T>): IndexTreeNode<T, R>? {
    var currentNode: IndexTreeNode<T, R> = this
    for (pathElement in path) {
        currentNode = currentNode[pathElement] ?: return null
    }
    return currentNode
}

fun <T : Any, R : Any> IndexTreeNode<T, R>.findBranchesForResult(
    predicate: (R?) -> Boolean
): Map<List<T>, R?> =
    findBranchesForResultInternal(predicate, listOf())

private fun <T : Any, R : Any> IndexTreeNode<T, R>.findBranchesForResultInternal(
    predicate: (R?) -> Boolean,
    currentBranchSequence: List<T>
): Map<List<T>, R?> {

    val ret = HashMap<List<T>, R?>()

    if (predicate(result)) {
        ret[currentBranchSequence] = result
    }

    children.forEach { (childId, childBranch) ->
        ret += childBranch.findBranchesForResultInternal(predicate, currentBranchSequence + childId)
    }

    return ret
}