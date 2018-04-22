package ast

import tokens.IDENTIFIER

abstract class VariableNode : DefinitionNode() {
    val hasInitializer: Boolean
        get() = initializer != null

    val initializer: ExpressionNode?
        get() = findChildByClass()

    val name: TokenNode?
        get() = findChildByToken(IDENTIFIER)
}
