package ast

class BinaryExpressionNode(position: Pair<Int, Int>) : ExpressionNode(position) {
    val operation: TokenNode
        get() = findChildByClass()!!

    val left: ExpressionNode
        get() = findChildByClass()!!

    val right: ExpressionNode
        get() = findChildByClassAfter(operation)!!
}
