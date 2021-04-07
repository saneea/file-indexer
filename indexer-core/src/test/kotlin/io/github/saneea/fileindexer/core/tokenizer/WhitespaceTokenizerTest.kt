package io.github.saneea.fileindexer.core.tokenizer

import org.junit.Test
import kotlin.test.assertEquals

class WhitespaceTokenizerTest {

    @Test
    fun testJustSpaces() {
        testString(
            " abc 1234 +-=     ",
            "abc", "1234", "+-="
        )
    }

    @Test
    fun testManyWhitespaces() {
        testString(
            " \t\tabc \n\n\n 1234 +-=   \r\r  ",
            "abc", "1234", "+-="
        )
    }

    @Test
    fun testEmptyString() {
        testString(
            ""
        )
    }

    @Test
    fun testWhitespacesWithoutTokens() {
        testString(
            "\t \t \t \n \r \n\n\r\r"
        )
    }

    @Test
    fun testOneToken1Char() {
        testOneToken("a")
    }

    @Test
    fun testOneToken2Char() {
        testOneToken("ab")
    }

    @Test
    fun testOneToken3Char() {
        testOneToken("abc")
    }

    private fun testOneToken(token: String) {
        testString(token, token)
    }

    private fun testString(str: String, vararg expectedTokens: String) {
        val actualTokens = parseString(str)
        assertEquals(expectedTokens.toList(), actualTokens)
    }

    private fun parseString(str: String): List<String> {
        val ret: MutableList<String> = ArrayList()

        val listener: TokenizerListener = object : TokenizerListener {
            override fun onToken(token: String) {
                ret.add(token)
            }
        }

        val tokenizer = WhitespaceTokenizer()

        str.reader().use { reader -> tokenizer.parse(reader, listener) }

        return ret
    }
}