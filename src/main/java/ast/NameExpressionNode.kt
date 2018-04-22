package ast

import tokens.IDENTIFIER

class NameExpressionNode : ExpressionNode() {
    val name: TokenNode?
        get() = findChildByToken(IDENTIFIER)
}
