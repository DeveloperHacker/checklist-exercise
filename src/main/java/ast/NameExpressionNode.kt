package ast

import tokens.IDENTIFIER

class NameExpressionNode(position: Pair<Int, Int>): ExpressionNode(position) {
    val name: String
        get() = findChildByToken(IDENTIFIER)!!.text
}
