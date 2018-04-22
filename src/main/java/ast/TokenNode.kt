package ast

import tokens.Token

class TokenNode(val token: Token, val text: String, position: Pair<Int, Int>) : Node(position) {
    override fun toString() = "${javaClass.name}($text)"
}
