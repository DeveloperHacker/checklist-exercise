package ast

class ConstantExpressionNode(position: Pair<Int, Int>) : ExpressionNode(position) {
    val value: TokenNode
        get() = findChildByClass()!!
}
