package io.github.saneea.fileindexer.core.utils.index

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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

    @Test
    fun testReuseBranch() {
        val branchesLetters = mapOf("abcd" to 1, "qwer" to 2)
        val tree1 = IndexTreeNodeBuilder<Char, Int>()
            .also { it.addStringBranches(branchesLetters) }
            .build()
            .also { it.verify(branchesLetters) }

        val subTree1A = tree1['a']
        val subTree1ABC = tree1['a']!!['b']
        val subTree1Q = tree1['q']

        subTree1A!!.verify(mapOf("bcd" to 1))
        subTree1ABC!!.verify(mapOf("cd" to 1))
        subTree1Q!!.verify(mapOf("wer" to 2))

        val branchesDigits = mapOf("1234" to 3, "5678" to 4)
        val tree2 = IndexTreeNodeBuilder(tree1)
            .also { it.addStringBranches(branchesDigits) }
            .build()
            .also { it.verify(branchesLetters + branchesDigits) }

        val subTree2A = tree2['a']
        val subTree2Q = tree2['q']

        assertSame(subTree1A, subTree2A)
        assertSame(subTree1Q, subTree2Q)

        val tree3 = IndexTreeNodeBuilder(tree2)
            .also { it.addStringBranch("axyz", 5) }
            .build()
            .also {
                it.verify(
                    branchesLetters +
                            branchesDigits +
                            mapOf("axyz" to 5)
                )
            }

        val subTree3ABC = tree3['a']!!['b']
        val subTree3Q = tree3['q']

        assertSame(subTree1Q, subTree3Q)
        assertSame(subTree1ABC, subTree3ABC)
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