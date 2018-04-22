import ast.FileNode
import ast.ParameterNode

typealias Parameters = Map<String, Any>

typealias Values = Set<String>

class Executor {
    private fun getParameters(file: FileNode, parameters: Parameters): Pair<Parameters, Values> {
        val variables = file.variables
                .filterIsInstance<ParameterNode>()
                .map { it to 0 }
                .toMap()
        val uninitializedParameter = variables.asSequence()
                .map { it.key }
                .filterNot { it.hasInitializer }
                .filterNot { parameters.containsKey(it.name!!.text) }
                .firstOrNull()
        uninitializedParameter?.let {
            val (line, column) = it.position
            val name = file.name
            val message = "Uninitialized parameter: ${it.name!!.text}"
            throw ProcessException("$name:[$line, $column] $message")
        }
        val result = variables.map { it.key.name!!.text to it.value }.toMap()
        return Pair(result, result.keys)
    }

    fun exec(storage: FileNode, checklist: FileNode): Triple<Values, Values, Values> {
        val (parameters, storageValues) = getParameters(storage, HashMap())
        val (_, checklistValues) = getParameters(checklist, parameters)
        val normal = storageValues.intersect(checklistValues)
        val underflow = checklistValues.minus(storageValues)
        val overflow = storageValues.minus(checklistValues)
        return Triple(normal, underflow, overflow)
    }
}
