package io.github.saneea.fileindexer.core.utils.index

import org.junit.Test
import kotlin.test.assertEquals

private val BRANCHES_1 = mapOf(
    "saneea" to 1,
    "sokol" to 2,
    "alexander" to 3,
    "alex" to 4,
    "source" to 5,
    "" to 6,
    "s" to 7,
    "z" to 8
)

private val BRANCHES_2 = mapOf(
    "smart" to 9,
    "sun" to 10,
    "file" to 11
)

class IndexTreeNodeBuilderTest {

    @Test
    fun testAddBranch() {
        IndexTreeNodeBuilder<Char, Int>()
            .also { it.addStringBranches(BRANCHES_1) }
            .build()
            .verify(BRANCHES_1)
    }

    @Test
    fun testExtendBranch() {
        val tree1 = IndexTreeNodeBuilder<Char, Int>()
            .also { it.addStringBranches(BRANCHES_1) }
            .build()
            .also { it.verify(BRANCHES_1) }

        IndexTreeNodeBuilder(tree1)
            .also { it.addStringBranches(BRANCHES_2) }
            .build()
            .also { it.verify(BRANCHES_1 + BRANCHES_2) }
    }

    @Test
    fun testRemoveBranch() {
        val tree1 = IndexTreeNodeBuilder<Char, Int>()
            .also { it.addStringBranches(BRANCHES_1) }
            .build()
            .also { it.verify(BRANCHES_1) }

        IndexTreeNodeBuilder(tree1)
            .also {
                it
                    .getOrCreateBranch("so".asIterable())
                    .removeChild('k')
            }
            .build()
            .verify(BRANCHES_1 - "sokol")
    }

    private fun IndexTreeNode<Char, Int>.verify(expected: Map<String, Int>) =
        assertEquals(expected, getTextBranches())

    private fun IndexTreeNode<Char, Int>.getTextBranches(): Map<String, Int> {
        val ret = HashMap<String, Int>()
        getTextBranches("", ret)
        return ret
    }

    private fun IndexTreeNode<Char, Int>.getTextBranches(
        currentBranchSequence: String,
        ret: MutableMap<String, Int>
    ) {

        val result = result
        if (result != null) {
            ret[currentBranchSequence] = result
        }

        for (childId in childIds) {
            this[childId]!!.getTextBranches(currentBranchSequence + childId, ret)
        }
    }

    private fun IndexTreeNodeBuilder<Char, Int>.addStringBranches(branches: Map<String, Int>) =
        branches.forEach { (k, v) ->
            addStringBranch(k, v)
        }

    private fun IndexTreeNodeBuilder<Char, Int>.addStringBranch(str: String, res: Int) {
        this.getOrCreateBranch(str.asIterable(), res)
    }
}