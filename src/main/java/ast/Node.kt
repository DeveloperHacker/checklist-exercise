package ast

abstract class Node {
    var parent: Node? = null
        private set

    val children: List<Node>
        get() = mutableChildren.toList()

    private val mutableChildren = ArrayList<Node>()

    fun addChild(child: Node) {
        mutableChildren.add(child)
        child.parent = this
    }

    open val text: String
        get() = javaClass.name
}