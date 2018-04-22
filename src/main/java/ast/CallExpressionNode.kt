package ast

import tokens.IDENTIFIER

class CallExpressionNode(position: Pair<Int, Int>) : ExpressionNode(position) {
    val name: String
        get() = findChildByToken(IDENTIFIER)!!.text

    val arguments: List<ExpressionNode>
        get() = findChildrenByClass()
}
