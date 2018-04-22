package ast

class ParenthesizedExpressionNode(position: Pair<Int, Int>) : ExpressionNode(position) {
    val expression: ExpressionNode
        get() = findChildByClass()!!
}
