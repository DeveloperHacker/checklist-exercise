package ast

import tokens.LBRACE
import tokens.LPAR
import tokens.RBRACE
import tokens.RPAR

class BlockNode(position: Pair<Int, Int>) : Node(position) {

    private val leftBrace: TokenNode
        get() = findChildByToken(LBRACE)!!

    private val rightBrace: TokenNode
        get() = findChildByToken(RBRACE)!!

    val statements: List<Node>
        get() = findChildrenBetween(leftBrace, rightBrace)
}
