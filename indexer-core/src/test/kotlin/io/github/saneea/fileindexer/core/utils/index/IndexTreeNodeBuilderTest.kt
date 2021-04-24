package io.github.saneea.fileindexer.core.utils.index

import org.junit.Test
import kotlin.test.assertEquals

class IndexTreeNodeBuilderTest {

    @Test
    fun testAddingBranch() {

        val builder = IndexTreeNodeBuilder<Char, Int>()

        val expected = mapOf(
            "saneea" to 1,
            "sokol" to 2,
            "alexander" to 3,
            "alex" to 4,
            "source" to 5,
        )

        expected.forEach { (k, v) ->
            builder.addStringBranch(k, v)
        }

        val treeNode = builder.build()

        treeNode.verifyTree(expected)
    }

    private fun IndexTreeNode<Char, Int>.verifyTree(expected: Map<String, Int>) =
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

    private fun IndexTreeNodeBuilder<Char, Int>.addStringBranch(str: String, res: Int) {
        this.getOrCreateBranch(str.asIterable(), res)
    }
}