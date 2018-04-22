package ast

class FileNode(val name: String, position: Pair<Int, Int>) : Node(position) {
    val variables: List<VariableNode>
        get() = findChildrenByClass()

    val parameters: List<ParameterNode>
        get() = findChildrenByClass()

    val values: List<ParameterNode>
        get() = findChildrenByClass()

    val functions: List<FunctionNode>
        get() = findChildrenByClass()

    val executable: List<Node>
        get() = findChildrenBy {
            it is VariableNode || it is ExpressionNode || it is IfStatementNode
        }
}
