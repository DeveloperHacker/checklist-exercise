package ast

import tokens.IDENTIFIER

abstract class VariableNode(position: Pair<Int, Int>): DefinitionNode(position) {
    val hasInitializer: Boolean
        get() = initializer != null

    val initializer: ExpressionNode?
        get() = findChildByClass()

    val name: String
        get() = findChildByToken(IDENTIFIER)!!.text
}
