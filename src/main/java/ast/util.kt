package ast

import tokens.Token


fun Node.findChildByToken(token: Token): TokenNode? =
        children.firstOrNull { it is TokenNode && it.token == token } as TokenNode?

inline fun <reified T : Node> Node.findChildByClass(): T? =
        children.firstOrNull { it is T } as? T

inline fun <reified T : Node> Node.findChildByClassAfter(node: Node) =
        children.dropWhile { it != node }
                .dropWhile { it == node }
                .firstOrNull { it is T } as? T

inline fun <reified T : Node> Node.findChildrenByClass(): List<T> =
        children.filterIsInstance<T>().toList()

fun Node.findChildrenBy(predicate: (Node) -> Boolean): List<Node> =
        children.filter(predicate)

fun Node.findChildrenBetween(left: Node, right: Node) =
        children.dropWhile { it != left }
                .dropWhile { it == left }
                .dropLastWhile { it != right }
                .dropLastWhile { it == right }

fun Node.findChildrenByTokenBetween(left: Node, right: Node, token: Token) =
        children.dropWhile { it != left }
                .dropWhile { it == left }
                .dropLastWhile { it != right }
                .dropLastWhile { it == right }
                .filter { it is TokenNode && it.token == token }
                .filterIsInstance<TokenNode>()

fun Node.println() {
    println("---BEGIN PSI STRUCTURE---")
    println(0)
    println("---END PSI STRUCTURE---")
}

fun Node.println(indentation: Int) {
    println("|".repeat(indentation) + toString())
    for (child in children)
        child.println(indentation + 1)
    if (children.isEmpty())
        println("|".repeat(indentation + 1) + "$this")
}