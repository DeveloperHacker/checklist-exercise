package ast

import tokens.ELSE
import tokens.LPAR
import tokens.RPAR

class IfStatementNode(position: Pair<Int, Int>) : Node(position) {

    private val leftPar: TokenNode
        get() = findChildByToken(LPAR)!!

    private val rightPar: TokenNode
        get() = findChildByToken(RPAR)!!

    private val elseKeyword: TokenNode?
        get() = findChildByToken(ELSE)

    val condition: ExpressionNode
        get() = findChildByClassAfter(leftPar)!!

    val thenBlockOrExpression: Node
        get() = findChildByClassAfter(rightPar)!!

    val elseBlockOrExpression: Node?
        get() = elseKeyword?.let { findChildByClassAfter(it) }
}
