package ast

import tokens.Token

class TokenNode(val token: Token, val value: String) : Node() {
    override val text: String
        get() = "${javaClass.name}(${token.name})"
}
