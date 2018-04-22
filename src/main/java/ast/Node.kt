package ast

abstract class Node(val position: Pair<Int, Int>) {
    var parent: Node? = null
        private set

    val children: List<Node>
        get() = mutableChildren.toList()

    private val mutableChildren = ArrayList<Node>()

    fun addChild(child: Node) {
        mutableChildren.add(child)
        child.parent = this
    }

    override fun toString() = javaClass.name.toString()
}