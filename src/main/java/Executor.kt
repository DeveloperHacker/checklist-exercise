import ast.FileNode
import ast.ParameterNode

typealias Parameters = Map<String, Any>

typealias Values = Set<String>

class Executor {
    private fun getParameters(file: FileNode, parameters: Parameters): Pair<Parameters, Values> {
        val newParameters = file.variables
                .filterIsInstance<ParameterNode>()
                .map { it.name!!.value to 0 }
                .toMap()
        val uninitializedParameter = newParameters.asSequence()
                .map(Map.Entry<String, Int>::key)
                .filterNot(parameters::containsKey)
                .firstOrNull()
        uninitializedParameter?.let {
            throw ProcessException("Parameter '$it' is not initialized in file '${file.name}'")
        }
        return Pair(newParameters, newParameters.keys)
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
