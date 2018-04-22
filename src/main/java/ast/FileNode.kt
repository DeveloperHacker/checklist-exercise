package ast

class FileNode(val name: String, position: Pair<Int, Int>): Node(position) {
    val variables: List<VariableNode>
        get() = findChildrenByClass()
}
