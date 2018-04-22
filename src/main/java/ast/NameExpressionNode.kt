package ast

import tokens.IDENTIFIER

class NameExpressionNode(position: Pair<Int, Int>): ExpressionNode(position) {
    val name: TokenNode?
        get() = findChildByToken(IDENTIFIER)
}
