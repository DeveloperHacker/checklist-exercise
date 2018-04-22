package ast

import tokens.IDENTIFIER
import tokens.LPAR
import tokens.RPAR

class FunctionNode(position: Pair<Int, Int>) : DefinitionNode(position) {
    val name: String
        get() = findChildByToken(IDENTIFIER)!!.text

    private val leftPar: TokenNode
        get() = findChildByToken(LPAR)!!

    private val rightPar: TokenNode
        get() = findChildByToken(RPAR)!!

    val body: ExpressionNode
        get() = findChildByClass()!!

    val arguments: List<TokenNode>
        get() = findChildrenByTokenBetween(leftPar, rightPar, IDENTIFIER)
}
