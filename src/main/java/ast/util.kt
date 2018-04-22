package ast

import tokens.Token

fun Node.findChildByToken(token: Token): TokenNode? =
        children.firstOrNull { it is TokenNode && it.token == token } as TokenNode?

inline fun <reified T : Node> Node.findChildByClass(): T? =
        children.firstOrNull { it is T } as? T

inline fun <reified T : Node> Node.findChildrenByClass(): List<T> =
        children.filterIsInstance<T>().toList()

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
        println("|".repeat(indentation + 1) + "'$text'")
}