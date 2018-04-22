package ast

class FileNode(val name: String) : Node() {
    val variables: List<VariableNode>
        get() = findChildrenByClass()
}
