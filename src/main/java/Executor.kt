import ast.*
import tokens.*
import java.util.*

sealed class Object<out T : Any>(val value: T)
class IntegerObject(value: Int) : Object<Int>(value)
class StringObject(value: String) : Object<String>(value)
class BooleanObject(value: Boolean) : Object<Boolean>(value)

data class Function(val node: FunctionNode) {
    override fun toString(): String {
        val name = node.name
        val arguments = node.arguments.map { it.text }
        return "$name(${arguments.joinToString(", ")})"
    }
}

data class Parameter(val node: ParameterNode, val value: Object<*>)

data class Value(val node: ValueNode, val value: Object<*>)

data class Argument(val node: TokenNode, val value: Object<*>)

data class Environment(
        val functions: Map<String, Function>,
        val values: MutableMap<String, Value>,
        val parameters: MutableMap<String, Parameter>,
        val arguments: Map<String, Argument>
) {
    fun replaceArguments(arguments: Map<String, Argument>) =
            Environment(functions, values, parameters, arguments)
}

data class Result(val parameters: Map<String, Parameter>, val values: List<String>)

class Executor {
    private fun error(file: FileNode, node: Node, message: String): Nothing {
        val (line, column) = node.position
        val name = file.name
        throw ProcessException("$name:[$line, $column] $message")
    }

    private inline fun <reified T : Any> Object<*>?.checkType(file: FileNode, node: Node) =
            this?.value as? T
                    ?: error(file, node, "Result of expression must have a ${T::class.java.name} type")

    private fun valueOf(file: FileNode, node: Node, value: Any): Object<*> =
            when (value) {
                is Int -> IntegerObject(value)
                is String -> StringObject(value)
                is Boolean -> BooleanObject(value)
                else -> error(file, node, "Unsupported type: ${value.javaClass.name}")
            }

    // ToDo(sergei) do it
    private fun processBinaryExpression(
            file: FileNode,
            expression: BinaryExpressionNode,
            environment: Environment
    ): Object<*> {
        val left = expression.left
        val right = expression.right
        val exec: (ExpressionNode) -> Object<*> = { exec(file, it, environment) }
        val leftValue = exec(left)
        val operation = expression.operation
        val resultValue: Any = when (operation.token) {
            EQ_EQ -> leftValue.value == exec(right).value
            EXCL_EQ -> leftValue.value != exec(right).value
            PLUS -> {
                val rightValue = exec(right)
                when (leftValue) {
                    is StringObject -> leftValue.value + rightValue.value
                    is BooleanObject -> error(file, operation, "Unsupported operation: ${PLUS.value}")
                    is IntegerObject -> when (rightValue) {
                        is StringObject -> leftValue.value.toString() + rightValue.value
                        is BooleanObject -> error(file, operation, "Unsupported operation: ${PLUS.value}")
                        is IntegerObject -> leftValue.value + rightValue.value
                    }
                }
            }
            MINUS -> {
                val rightValue = exec(right)
                if (leftValue !is IntegerObject || rightValue !is IntegerObject)
                    error(file, operation, "Unsupported operation: ${MINUS.value}")
                leftValue.value - rightValue.value
            }
            MUL -> {
                val rightValue = exec(right)
                when (leftValue) {
                    is StringObject -> when (rightValue) {
                        is IntegerObject -> leftValue.value.repeat(rightValue.value)
                        else -> error(file, operation, "Unsupported operation: ${MUL.value}")
                    }
                    is BooleanObject -> error(file, operation, "Unsupported operation: ${MUL.value}")
                    is IntegerObject -> when (rightValue) {
                        is IntegerObject -> leftValue.value * rightValue.value
                        else -> error(file, operation, "Unsupported operation: ${MUL.value}")
                    }
                }
            }
            AND_AND -> {
                if (leftValue !is BooleanObject)
                    error(file, operation, "Unsupported operation: ${AND_AND.value}")
                when (leftValue.value) {
                    false -> false
                    true -> exec(right).value as? Boolean
                            ?: error(file, operation, "Unsupported operation: ${AND_AND.value}")
                }
            }
            OR_OR -> {
                if (leftValue !is BooleanObject)
                    error(file, operation, "Unsupported operation: ${OR_OR.value}")
                when (leftValue.value) {
                    true -> true
                    false -> exec(right).value as? Boolean
                            ?: error(file, operation, "Unsupported operation: ${OR_OR.value}")
                }
            }
            LT -> {
                val rightValue = exec(right)
                when {
                    leftValue is IntegerObject && rightValue is IntegerObject ->
                        leftValue.value < rightValue.value
                    leftValue is StringObject && rightValue is StringObject ->
                        leftValue.value < rightValue.value
                    else -> error(file, operation, "Unsupported operation: ${LT.value}")
                }
            }
            GT -> {
                val rightValue = exec(right)
                when {
                    leftValue is IntegerObject && rightValue is IntegerObject ->
                        leftValue.value > rightValue.value
                    leftValue is StringObject && rightValue is StringObject ->
                        leftValue.value > rightValue.value
                    else -> error(file, operation, "Unsupported operation: ${GT.value}")
                }
            }
            else -> error(file, operation, "Undefined operation: $operation")
        }
        return valueOf(file, expression, resultValue)
    }

    private fun processConstantExpression(file: FileNode, expression: ConstantExpressionNode): Object<*> {
        val valueNode = expression.value
        val value = valueNode.text
        val result: Any = when (valueNode.token) {
            BOOLEAN -> value.toBoolean()
            INTEGER -> value.toInt()
            STRING -> value.drop(1).dropLast(1)
            else -> error(file, expression, "Unsupported constant: ${valueNode.token}")
        }
        return valueOf(file, expression, result)
    }

    private fun processCallExpression(
            file: FileNode,
            expression: CallExpressionNode,
            environment: Environment
    ): Object<*> {
        val name = expression.name
        val function = environment.functions[name]
                ?: error(file, expression, "Unresolved reference: $name")
        val argumentNames = function.node.arguments.map { it }
        val argumentValues = expression.arguments.map { it }
        if (argumentNames.size > argumentValues.size)
            error(file, expression, "Too many arguments: $function")
        else if (argumentNames.size < argumentValues.size) {
            val diff = argumentNames.drop(argumentValues.size).map { it.text }
            error(file, expression, "No value passed for arguments: ${diff.joinToString(", ")}")
        }
        val arguments = argumentNames.zip(argumentValues)
                .mapSecond { exec(file, it, environment) }
                .map { (it1, it2) -> it1 to Argument(it1, it2) }
                .mapFirst { it.text }
                .toMap()
        val body = function.node.body
        return exec(file, body, environment.replaceArguments(arguments))
    }

    private fun processNameExpression(
            file: FileNode,
            expression: NameExpressionNode,
            environment: Environment
    ): Object<*> {
        val name = expression.name
        environment.arguments[name]?.let { return it.value }
        environment.values[name]?.let { return it.value }
        environment.parameters[name]?.let { return it.value }
        error(file, expression, "Unresolved reference: $name")
    }

    private fun processIfExpression(
            file: FileNode,
            expression: IfExpressionNode,
            environment: Environment
    ): Object<*> {
        val condition = expression.condition
        val thenExpression = expression.thenExpression
        val elseExpression = expression.elseExpression
        val conditionValue = exec(file, condition, environment).checkType<Boolean>(file, condition)
        val branchExpression = if (conditionValue) thenExpression else elseExpression
        return exec(file, branchExpression, environment)
    }

    private fun processParenthesizedExpression(
            file: FileNode,
            expression: ParenthesizedExpressionNode,
            environment: Environment
    ) = exec(file, expression.expression, environment)

    private fun exec(file: FileNode, expression: ExpressionNode, environment: Environment): Object<*> =
            when (expression) {
                is BinaryExpressionNode -> processBinaryExpression(file, expression, environment)
                is CallExpressionNode -> processCallExpression(file, expression, environment)
                is ConstantExpressionNode -> processConstantExpression(file, expression)
                is IfExpressionNode -> processIfExpression(file, expression, environment)
                is NameExpressionNode -> processNameExpression(file, expression, environment)
                is ParenthesizedExpressionNode -> processParenthesizedExpression(file, expression, environment)
                else -> error(file, expression, "Undefined expression")
            }

    private fun processParameter(
            file: FileNode,
            it: ParameterNode,
            parameters: Map<String, Parameter>,
            environment: Environment
    ) {
        val name = it.name
        if (environment.parameters.containsKey(name))
            error(file, it, "Parameter redefinition: $name")
        val parameter = parameters[name]
        if (parameter != null) {
            val value = parameter.value
            environment.parameters[name] = Parameter(it, value)
            return
        }
        val initializer = it.initializer
        if (initializer != null) {
            val value = exec(file, initializer, environment)
            environment.parameters[name] = Parameter(it, value)
            return
        }
        error(file, it, "Uninitialized parameter: $name")
    }

    private fun processValue(file: FileNode, it: ValueNode, environment: Environment) {
        val name = it.name
        if (environment.values.containsKey(name))
            error(file, it, "Value redefinition: $name")
        val initializer = it.initializer
        if (initializer != null) {
            val value = exec(file, initializer, environment)
            environment.values[name] = Value(it, value)
            return
        }
        error(file, it, "Uninitialized value: $name")
    }

    private fun processExpression(file: FileNode, it: ExpressionNode, environment: Environment) =
            exec(file, it, environment).checkType<String>(file, it)


    private fun processIfStatement(file: FileNode, it: IfStatementNode, environment: Environment): List<String> {
        val condition = it.condition
        val thenBlock = it.thenBlockOrExpression
        val elseBlock = it.elseBlockOrExpression
        val conditionValue = exec(file, condition, environment).checkType<Boolean>(file, condition)
        return if (conditionValue) when (thenBlock) {
            is ExpressionNode -> listOf(processExpression(file, thenBlock, environment))
            is BlockNode -> processBlock(file, thenBlock, environment)
            is IfStatementNode -> processIfStatement(file, thenBlock, environment)
            else -> error(file, thenBlock, "Undefined statement: $thenBlock")
        } else when (elseBlock) {
            null -> listOf()
            is ExpressionNode -> listOf(processExpression(file, elseBlock, environment))
            is BlockNode -> processBlock(file, elseBlock, environment)
            is IfStatementNode -> processIfStatement(file, elseBlock, environment)
            else -> error(file, elseBlock, "Undefined statement: $elseBlock")
        }
    }

    private fun processStatement(file: FileNode, it: Node, environment: Environment) =
            when (it) {
                is ExpressionNode -> listOf(processExpression(file, it, environment))
                is IfStatementNode -> processIfStatement(file, it, environment)
                else -> error(file, it, "Undefined statement")
            }

    private fun processBlock(file: FileNode, block: BlockNode, environment: Environment) =
            block.statements
                    .map { processStatement(file, it, environment) }
                    .fold(listOf<String>()) { acc, it -> acc + it }

    private fun process(file: FileNode, parameters: Map<String, Parameter>): Result {
        val functions = file.functions.map { it.name to Function(it) }.toMap()
        val environment = Environment(functions, HashMap(), HashMap(), HashMap())
        val results = ArrayList<String>()
        loop@ for (it in file.executable) {
            when (it) {
                is ParameterNode -> processParameter(file, it, parameters, environment)
                is ValueNode -> processValue(file, it, environment)
                else -> results.addAll(processStatement(file, it, environment))
            }
        }
        return Result(environment.parameters, results)
    }

    fun exec(storage: FileNode, checklist: FileNode): List<String> {
        val (parameters, storageValues) = process(storage, HashMap())
        val (_, checklistValues) = process(checklist, parameters)
        return checklistValues + "\n" + storageValues
    }
}
