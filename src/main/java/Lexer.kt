import tokens.*

class LexerException(val position: Int, message: String) : Exception(message)

class Lexer(private val data: String) {

    private var offset = 0

    private lateinit var currentToken: Token

    private lateinit var currentTokenText: String

    init {
        nextToken()
    }

    private fun whiteSpaceLength() = rawTokenText().count { it in whiteSpaces }

    private fun rawTokenText(): String {
        val end = data.indexOf(' ', offset)
        if (end == -1) return data.substring(offset)
        return data.substring(offset, end)
    }

    private fun advanceImmutableTokenOrNull(): Pair<ValueToken, String>? {
        val current = rawTokenText()
        val token = valueTokens.firstOrNull { current == it.value } ?: return null
        return Pair(token, current)
    }

    private fun advanceIntegerTokenOrNull(): Pair<Token, String>? {
        val current = rawTokenText()
        current.toIntOrNull() ?: return null
        return Pair(INTEGER, current)
    }

    private fun advanceStringTokenOrNull(): Pair<Token, String>? {
        val regex = "\"[^(\"|\\n)]\"*".toRegex()
        val current = regex.find(data, offset) ?: return null
        return Pair(STRING, current.value)
    }

    private fun advanceBooleanTokenOrNull(): Pair<Token, String>? {
        val current = rawTokenText()
        if (current != "true" && current != "false") return null
        return Pair(BOOLEAN, current)
    }

    private fun advanceIdentifierTokenOrNull(): Pair<Token, String>? {
        val current = rawTokenText()
        if (!current.matches("[a-zA-Z_][a-zA-Z_0-9]*".toRegex())) return null
        return Pair(IDENTIFIER, current)
    }

    private fun error(message: String): Nothing = throw LexerException(position(), message)

    private fun nextToken() {
        offset += whiteSpaceLength()
        val (token, text) = run {
            if (eof()) error("Expected currentToken but instead EOF")
            advanceImmutableTokenOrNull()?.let { return@run it }
            advanceIntegerTokenOrNull()?.let { return@run it }
            advanceStringTokenOrNull()?.let { return@run it }
            advanceBooleanTokenOrNull()?.let { return@run it }
            advanceIdentifierTokenOrNull()?.let { return@run it }
            error("valueTokens.Token ${rawTokenText()}' hasn't recognized")
        }
        currentToken = token
        currentTokenText = text
    }

    fun position() = offset

    fun token() = currentToken

    fun tokenText() = currentTokenText

    fun eof() = offset + currentTokenText.length + whiteSpaceLength() == data.length

    fun at(expected: Token) = token() == expected

    fun advance() {
        offset += currentTokenText.length
        nextToken()
    }
}