import tokens.*

class LexerException(val position: Pair<Int, Int>, message: String) : Exception(message)

class Lexer(private val data: String) {

    private var offset = 0

    private var isEOF = false

    private lateinit var currentToken: Token

    private lateinit var currentTokenText: String

    private fun whiteSpaceLength(): Int {
        val data = data.substring(offset)
        var counter = 0
        for (char in data) {
            if (char !in whiteSpaces) break
            ++counter
        }
        return counter
    }

    private fun commentLength(comment: Pair<String, String>): Int {
        val (first, second) = comment
        val data = data.substring(offset)
        if (!data.startsWith(first)) return 0
        val index = data.indexOf(second)
        return if (index == -1) data.length else index + second.length
    }

    private fun shift(value: Int) {
        offset += value
        offset += whiteSpaceLength()
        var modify = true
        while (modify) {
            modify = false
            val complexCommentLength = commentLength(complexComment)
            val simpleCommentLength = commentLength(simpleComment)
            val commentLength = maxOf(simpleCommentLength, complexCommentLength)
            if (commentLength > 0) {
                offset += commentLength
                offset += whiteSpaceLength()
                modify = true
            }
        }
    }

    private fun rawTokenText(): String {
        val data = data.substring(offset)
        val firstWhiteSpace = whiteSpaces
                .map { data.indexOf(it) }
                .map { if (it == -1) data.length else it }
                .min()!!
        val firstSymbol = symbols
                .map { data.indexOf(it) }
                .map { if (it == -1) data.length else it }
                .min()!!
        if (firstSymbol == 0) {
            val firstNonSymbol = data.indexOfFirst { it !in symbols }
            if (firstNonSymbol == -1) return data
            return data.substring(0, firstNonSymbol)
        }
        return data.substring(0, minOf(firstWhiteSpace, firstSymbol))
    }

    private fun advanceValueTokenOrNull(): Pair<ValueToken, String>? {
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
        val regex = "\"[^(\"|\n)]*\"".toRegex()
        val current = regex.find(data, offset)?.value ?: return null
        if (!data.substring(offset).startsWith(current)) return null
        return Pair(STRING, current)
    }

    private fun advanceBooleanTokenOrNull(): Pair<Token, String>? {
        val current = rawTokenText()
        if (current != "true" && current != "false") return null
        return Pair(BOOLEAN, current)
    }

    private fun advanceIdentifierTokenOrNull(): Pair<Token, String>? {
        val current = rawTokenText()
        if (!current.matches("[a-zA-Zа-яА-Я_][a-zA-Zа-яА-Я_0-9]*".toRegex())) return null
        return Pair(IDENTIFIER, current)
    }

    private fun error(message: String): Nothing = throw LexerException(position(), message)

    fun advance() {
        if (!isEOF && offset == data.length) {
            isEOF = true
            currentToken = EOF
            currentTokenText = ""
            return
        }
        val (token, text) = run {
            if (eof()) error("Expected token but instead EOF")
            advanceValueTokenOrNull()?.let { return@run it }
            advanceIntegerTokenOrNull()?.let { return@run it }
            advanceStringTokenOrNull()?.let { return@run it }
            advanceBooleanTokenOrNull()?.let { return@run it }
            advanceIdentifierTokenOrNull()?.let { return@run it }
            error("Token '${rawTokenText()}' hasn't recognized")
        }
        currentToken = token
        currentTokenText = text
        shift(text.length)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun context(): String {
        val head = data.substring(maxOf(0, offset - 10), offset)
        val tail = data.substring(offset, minOf(data.length, offset + 10))
        return "$head<~?~>$tail".replace("\n", "\\n")
    }

    fun position(): Pair<Int, Int> {
        val lines = data.split("\n")
        var unresolved = offset
        for (i in 0 until lines.size) {
            val line = lines[i]
            if (unresolved <= line.length)
                return Pair(i + 1, unresolved + 1)
            unresolved -= line.length + 1
        }
        return Pair(lines.size + 1, unresolved + 1)
    }

    fun token() = currentToken

    fun tokenText() = currentTokenText

    fun eof() = isEOF

    fun at(expected: Token) = !eof() && token() == expected

    inner class Marker {
        private val offset = this@Lexer.offset
        private val isEOF = this@Lexer.isEOF
        private val currentToken = this@Lexer.currentToken
        private val currentTokenText = this@Lexer.currentTokenText

        fun rollback() {
            this@Lexer.offset = offset
            this@Lexer.isEOF = isEOF
            this@Lexer.currentToken = currentToken
            this@Lexer.currentTokenText = currentTokenText
        }
    }

    fun mark() = Marker()

    init {
        shift(0)
        advance()
    }
}