import ast.*
import tokens.*
import tokens.Token

class ParserException(val position: Pair<Int, Int>, message: String) : Exception(message)

class Parser {

    private lateinit var lexer: Lexer

    private fun position() = lexer.position()

    private fun eof() = lexer.eof()

    private fun token() = lexer.token()

    private fun tokenText() = lexer.tokenText()

    private fun at(token: Token) = lexer.at(token)

    private fun mark() = lexer.mark()

    private fun atSet(tokens: Set<Token>) = tokens.any(lexer::at)

    private fun advance(element: Node) {
        val node = TokenNode(token(), tokenText(), position())
        element.addChild(node)
        lexer.advance()
    }

    private fun error(message: String): Nothing = throw ParserException(position(), message)

    private fun expected(element: Node, token: Token) {
        if (!at(token)) error("Expected token $token instead ${token()}(${tokenText()})")
        advance(element)
    }

    private fun unexpected(): Nothing = error("Unexpected token: ${token()}(${tokenText()})")

    fun parse(name: String, data: String): FileNode {
        try {
            lexer = Lexer(data)
            val file = FileNode(name, position())
            while (!eof()) {
                val definition = parseDefinition()
                file.addChild(definition)
            }
            return file
        } catch (ex: LexerException) {
            val (line, column) = ex.position
            throw ProcessException("$name:[$line, $column] ${ex.message}")
        } catch (ex: ParserException) {
            val (line, column) = ex.position
            throw ProcessException("$name:[$line, $column] ${ex.message}")
        }
    }

    private fun parseCallExpression(): ExpressionNode {
        val marker = mark()
        val nameExpression = NameExpressionNode(position())
        expected(nameExpression, IDENTIFIER)
        if (!at(LPAR)) return nameExpression
        marker.rollback()
        val callExpression = CallExpressionNode(position())
        expected(nameExpression, IDENTIFIER)
        expected(callExpression, LPAR)
        while (!at(RPAR)) {
            callExpression.addChild(parseExpression())
            if (!at(COMMA)) break
            expected(callExpression, COMMA)
        }
        expected(callExpression, RPAR)
        return callExpression
    }

    private fun parseStringExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode(position())
        expected(constantExpression, STRING)
        return constantExpression
    }

    private fun parseIntegerExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode(position())
        expected(constantExpression, INTEGER)
        return constantExpression
    }

    private fun parseBooleanExpression(): ConstantExpressionNode {
        val constantExpression = ConstantExpressionNode(position())
        expected(constantExpression, BOOLEAN)
        return constantExpression
    }

    private fun parseIfExpression(): IfExpressionNode {
        val ifExpression = IfExpressionNode(position())
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
            val binaryExpression = BinaryExpressionNode(position())
            binaryExpression.addChild(expression)
            advance(binaryExpression)
            binaryExpression.addChild(parseBinaryExpression(precedence))
            expression = binaryExpression
        }
        return expression
    }

    private fun parseExpression() = parseBinaryExpression(Int.MAX_VALUE)

    private fun parseValueDefinition(): ValueNode {
        val definition = ValueNode(position())
        expected(definition, VAL)
        expected(definition, IDENTIFIER)
        expected(definition, EQ)
        definition.addChild(parseExpression())
        return definition
    }

    private fun parseParameterDefinition(): ParameterNode {
        val definition = ParameterNode(position())
        expected(definition, PAR)
        expected(definition, IDENTIFIER)
        if (at(EQ)) {
            expected(definition, EQ)
            definition.addChild(parseExpression())
        }
        return definition
    }

    private fun parseFunctionDefinition(): FunctionNode {
        val definition = FunctionNode(position())
        expected(definition, FUN)
        expected(definition, IDENTIFIER)
        expected(definition, LPAR)
        while (!at(RPAR)) {
            expected(definition, IDENTIFIER)
            if (!at(COMMA)) break
            expected(definition, COMMA)
        }
        expected(definition, RPAR)
        expected(definition, EQ)
        definition.addChild(parseExpression())
        return definition
    }

    private fun parseIfStatement(): IfStatementNode {
        val statement = IfStatementNode(position())
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
        val block = BlockNode(position())
        expected(block, LBRACE)
        while (!at(RBRACE))
            block.addChild(parseExpression())
        expected(block, RBRACE)
        return block
    }

    private fun parseDefinition(): Node {
        if (at(PAR)) return parseParameterDefinition()
        if (at(FUN)) return parseFunctionDefinition()
        if (at(VAL)) return parseValueDefinition()
        return parseStatement()
    }
}