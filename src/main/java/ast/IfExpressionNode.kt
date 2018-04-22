package ast

import tokens.ELSE
import tokens.LPAR
import tokens.RPAR

class IfExpressionNode(position: Pair<Int, Int>) : ExpressionNode(position) {
    private val leftPar: TokenNode
        get() = findChildByToken(LPAR)!!

    private val rightPar: TokenNode
        get() = findChildByToken(RPAR)!!

    private val elseKeyword: TokenNode
        get() = findChildByToken(ELSE)!!

    val condition: ExpressionNode
        get() = findChildByClassAfter(leftPar)!!

    val thenExpression: ExpressionNode
        get() = findChildByClassAfter(rightPar)!!

    val elseExpression: ExpressionNode
        get() = elseKeyword.let { findChildByClassAfter(it)!! }
}
