import ast.*
import tokens.*
import tokens.Token

class ParserException(val position: Int, message: String) : Exception(message)

class Parser {

    private lateinit var lexer: Lexer

    private fun position() = lexer.position()

    private fun eof() = lexer.eof()

    private fun token() = lexer.token()

    private fun tokenText() = lexer.tokenText()

    private fun at(token: Token) = lexer.at(token)

    private fun atSet(tokens: Set<Token>) = tokens.any(lexer::at)

    private fun advance(element: Node) {
        val node = TokenNode(token(), tokenText())
        element.addChild(node)
        lexer.advance()
    }

    private fun error(message: String): Nothing = throw ParserException(position(), message)

    private fun expected(element: Node, token: Token) {
        if (!at(token)) error("Expected token $token")
        advance(element)
    }

    private fun unexpected(): Nothing = error("Unexpected token ${tokenText()}")

    private fun extract(data: String, offset: Int): Pair<Int, Int> {
        val lines = data.split("\n")
        var unresolved = offset
        for (i in 0..lines.size) {
            val line = lines[i]
            if (unresolved < line.length)
                return Pair(i + 1, unresolved + 1)
            unresolved -= line.length - 1
        }
        return Pair(0, 0)
    }

    fun parse(name: String, data: String): FileNode {
        try {
            lexer = Lexer(data)
            val file = FileNode(name)
            while (!eof()) {
                val definition = parseDefinition()
                file.addChild(definition)
            }
            return file
        } catch (ex: LexerException) {
            val (line, column) = extract(data, ex.position)
            throw ProcessException("parsing failed ($line, $column) ${ex.message}")
        } catch (ex: ParserException) {
            val (line, column) = extract(data, ex.position)
            throw ProcessException("parsing failed ($line, $column) ${ex.message}")
        }
    }

    private fun parseCallExpression(): ExpressionNode {
        val nameExpression = NameExpressionNode()
        expected(nameExpression, IDENTIFIER)
        if (!at(LPAR)) return nameExpression
        val callExpression = CallExpressionNode()
        for (node in nameExpression.children)
            callExpression.addChild(node)
        expected(callExpression, LPAR)
        while (!at(RPAR))
            callExpression.addChild(parseExpression())
        expected(callExpression, RPAR)
        return callExpression
    }

    private fun parseStringExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode()
        expected(constantExpression, STRING)
        return constantExpression
    }

    private fun parseIntegerExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode()
        expected(constantExpression, INTEGER)
        return constantExpression
    }

    private fun parseBooleanExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode()
        expected(constantExpression, BOOLEAN)
        return constantExpression
    }

    private fun parseIfExpression(): IfExpressionNode {
        val ifExpression = IfExpressionNode()
        expected(ifExpression, IF)
        ifExpression.addChild(parseExpression())
        expected(ifExpression, ELSE)
        ifExpression.addChild(parseExpression())
        return ifExpression
    }

    private fun parseParenthesizedExpression(): ParenthesizedExpressionNode = TODO()

    private fun parseAtomicExpression() = when {
        at(IF) -> parseIfExpression()
        at(STRING) -> parseStringExpression()
        at(INTEGER) -> parseIntegerExpression()
        at(BOOLEAN) -> parseBooleanExpression()
        at(LPAR) -> parseParenthesizedExpression()
        at(IDENTIFIER) -> parseCallExpression()
        else -> unexpected()
    }

    private fun parseBinaryExpression(leftPrecedence: Int): ExpressionNode {
        var expression = parseAtomicExpression()
        while (atSet(binaryOperationTokens)) {
            val operation = token()
            val precedence = binaryOperationPrecedence[operation]!!
            if (leftPrecedence <= precedence) return expression
            val binaryExpression = BinaryExpressionNode()
            binaryExpression.addChild(expression)
            advance(binaryExpression)
            binaryExpression.addChild(parseBinaryExpression(precedence))
            expression = binaryExpression
        }
        return expression
    }

    private fun parseExpression() = parseBinaryExpression(Int.MAX_VALUE)

    private fun parseValueDefinition(): ValueNode {
        val definition = ValueNode()
        expected(definition, VAL)
        expected(definition, IDENTIFIER)
        expected(definition, EQ)
        definition.addChild(parseExpression())
        return definition
    }

    private fun parseParameterDefinition(): ParameterNode {
        val definition = ParameterNode()
        expected(definition, PAR)
        expected(definition, IDENTIFIER)
        if (at(EQ)) {
            expected(definition, EQ)
            definition.addChild(parseExpression())
        }
        return definition
    }

    private fun parseFunctionDefinition(): FunctionNode {
        val definition = FunctionNode()
        expected(definition, FUN)
        expected(definition, IDENTIFIER)
        expected(definition, LPAR)
        while (!at(RPAR))
            expected(definition, IDENTIFIER)
        expected(definition, RPAR)
        expected(definition, EQ)
        definition.addChild(parseExpression())
        return definition
    }

    private fun parseIfStatement(): IfStatementNode {
        val statement = IfStatementNode()
        expected(statement, IF)
        expected(statement, LPAR)
        statement.addChild(parseExpression())
        expected(statement, RPAR)
        statement.addChild(when {
            at(LBRACE) -> parseBlock()
            else -> parseStatement()
        })
        expected(statement, ELSE)
        statement.addChild(when {
            at(LBRACE) -> parseBlock()
            else -> parseStatement()
        })
        return statement
    }

    private fun parseStatement() = when {
        at(IF) -> parseIfStatement()
        else -> parseExpression()
    }

    private fun parseBlock(): BlockNode {
        val block = BlockNode()
        expected(block, LBRACE)
        while (!at(RBRACE))
            block.addChild(parseExpression())
        expected(block, RBRACE)
        return block
    }

    private fun parseEnumDefinition(): EnumNode {
        val definition = EnumNode()
        expected(definition, ENUM)
        expected(definition, IDENTIFIER)
        definition.addChild(parseBlock())
        return definition
    }

    private fun parseDefinition(): DefinitionNode {
        if (at(PAR)) return parseParameterDefinition()
        if (at(FUN)) return parseFunctionDefinition()
        if (at(ENUM)) return parseEnumDefinition()
        if (at(VAL)) return parseValueDefinition()
        unexpected()
    }
}