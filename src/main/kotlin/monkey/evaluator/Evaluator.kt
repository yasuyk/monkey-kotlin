package monkey.evaluator

import monkey.`object`.Boolean
import monkey.`object`.Builtin
import monkey.`object`.Environment
import monkey.`object`.Environment.Companion.newEnclosedEnvironment
import monkey.`object`.Error
import monkey.`object`.Function
import monkey.`object`.Hash
import monkey.`object`.HashKey
import monkey.`object`.HashPair
import monkey.`object`.Hashable
import monkey.`object`.Integer
import monkey.`object`.MonkeyArray
import monkey.`object`.MonkeyString
import monkey.`object`.Null
import monkey.`object`.Object
import monkey.`object`.ObjectType
import monkey.`object`.ReturnValue
import monkey.`object`.isError
import monkey.ast.ArrayLiteral
import monkey.ast.BlockStatement
import monkey.ast.Bool
import monkey.ast.CallExpression
import monkey.ast.Expression
import monkey.ast.ExpressionStatement
import monkey.ast.FunctionLiteral
import monkey.ast.HashLiteral
import monkey.ast.Identifier
import monkey.ast.IfExpression
import monkey.ast.IndexExpression
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.LetStatement
import monkey.ast.Node
import monkey.ast.PrefixExpression
import monkey.ast.Program
import monkey.ast.ReturnStatement
import monkey.ast.StringLiteral


val TRUE = Boolean(true)
val FALSE = Boolean(false)
val NULL = Null()

fun eval(node: Node?, env: Environment): Object? {
    return when (node) {
        is Program -> evalProgram(node, env)
        is ExpressionStatement -> eval(node.value, env)
        is BlockStatement -> evalBlockStatement(node, env)
        is ReturnStatement -> {
            val v = eval(node.value, env)
            if (v.isError()) v else ReturnValue(v)
        }
        is LetStatement -> {
            val v = eval(node.value, env)
            if (v.isError()) {
                return v
            }
            env[node.name.value] = v
            return null
        }
        is PrefixExpression -> {
            val v = eval(node.right, env)
            if (v.isError()) v else evalPrefixExpression(node.operator, v)
        }
        is InfixExpression -> {
            val left = eval(node.left, env)
            if (left.isError()) {
                return left
            }
            val right = eval(node.right, env)
            if (right.isError()) {
                return right
            }
            evalInfixExpression(node.operator, left, right)
        }
        is IfExpression -> evalIfExpression(node, env)
        is CallExpression -> {
            val function = eval(node.function, env)
            if (function.isError()) {
                return function
            }
            val args = evalExpressions(node.arguments, env)
            if (args.size == 1 && args[0].isError()) {
                return args[0]
            }
            return applyFunction(function, args)
        }
        is IndexExpression -> {
            val left = eval(node.left, env)
            if (left.isError()) {
                return left
            }
            val right = eval(node.index, env)
            if (right.isError()) {
                return right
            }
            evalIndexExpression(left, right)
        }
        is Identifier -> evalIdentifier(node, env)
        is IntegerLiteral -> Integer(node.value)
        is StringLiteral -> MonkeyString(node.value)
        is FunctionLiteral -> Function(node.parameters, node.body, env)
        is ArrayLiteral -> {
            val elements = evalExpressions(node.elements, env)
            return if (elements.size == 1 && elements[0].isError()) {
                elements[0]
            } else {
                MonkeyArray(elements)
            }
        }
        is HashLiteral -> evalHashLiteral(node, env)
        is Bool -> nativeBoolToBooleanObject(node.value)
        else -> null
    }
}

private fun evalProgram(program: Program, env: Environment): Object? {
    var result: Object? = null
    for (stmt in program.statements) {
        result = eval(stmt, env)
        when (result) {
            is ReturnValue -> return result.value
            is Error -> return result
        }
    }
    return result
}

fun nativeBoolToBooleanObject(value: kotlin.Boolean) = if (value) TRUE else FALSE

fun evalPrefixExpression(operator: String, right: Object?): Object? {
    return when (operator) {
        "!" -> evalBangOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> Error("unknown operator: $operator${right?.type()}")
    }
}

fun evalBangOperatorExpression(right: Object?): Object? {
    return when (right) {
        TRUE -> return FALSE
        FALSE -> return TRUE
        NULL -> TRUE
        else -> FALSE
    }
}

fun evalMinusPrefixOperatorExpression(right: Object?): Object? {
    if (right?.type() != ObjectType.INTEGER) {
        return Error("unknown operator: -${right?.type()}")
    }

    val int = right as Integer
    return Integer(-int.value)
}

fun evalInfixExpression(operator: String, left: Object?, right: Object?): Object? {
    return when {
        left?.type() == ObjectType.INTEGER && right?.type() == ObjectType.INTEGER -> {
            evalIntegerInfixExpression(operator, left, right)
        }
        left?.type() == ObjectType.STRING && right?.type() == ObjectType.STRING -> {
            evalStringInfixExpression(operator, left, right)
        }
        operator == "==" -> nativeBoolToBooleanObject(left == right)
        operator == "!=" -> nativeBoolToBooleanObject(left != right)
        left?.type() != right?.type() -> Error("type mismatch: ${left?.type()} $operator ${right?.type()}")
        else -> Error("unknown operator: ${left?.type()} $operator ${right?.type()}")
    }
}

fun evalIntegerInfixExpression(operator: String, left: Object, right: Object): Object? {
    val leftVal = left as Integer
    val rightVal = right as Integer
    return when (operator) {
        "+" -> Integer(leftVal.value + rightVal.value)
        "-" -> Integer(leftVal.value - rightVal.value)
        "*" -> Integer(leftVal.value * rightVal.value)
        "/" -> Integer(leftVal.value / rightVal.value)
        "<" -> nativeBoolToBooleanObject(leftVal.value < rightVal.value)
        ">" -> nativeBoolToBooleanObject(leftVal.value > rightVal.value)
        "==" -> nativeBoolToBooleanObject(leftVal.value == rightVal.value)
        "!=" -> nativeBoolToBooleanObject(leftVal.value != rightVal.value)
        else -> Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }
}

fun evalStringInfixExpression(operator: String, left: Object, right: Object): Object? {
    if (operator != "+") {
        return Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }

    val leftVal = left as MonkeyString
    val rightVal = right as MonkeyString
    return MonkeyString(leftVal.value + rightVal.value)
}


fun evalIfExpression(ie: IfExpression, env: Environment): Object? {
    val condition = eval(ie.condition, env)
    if (condition.isError()) {
        return condition
    }
    return when {
        isTruthy(condition) -> eval(ie.consequence, env)
        ie.alternative != null -> eval(ie.alternative, env)
        else -> NULL
    }
}

fun evalExpressions(expressions: List<Expression>, env: Environment): List<Object?> {

    val result = mutableListOf<Object?>()
    for (e in expressions) {
        val evaluated = eval(e, env)
        if (evaluated.isError()) {
            return listOf(evaluated)
        }
        result.add(evaluated)
    }
    return result
}

fun applyFunction(fn: Object?, args: List<Object?>): Object? {
    return when (fn) {
        is Function -> {
            val extendedEnv = extendFunctionEnv(fn, args)
            val evaluated = eval(fn.body, extendedEnv)
            return unwrapReturnValue(evaluated)
        }
        is Builtin -> fn.fn(args)
        else -> Error("not a function: ${fn?.type()}")
    }
}

fun extendFunctionEnv(fn: Function, args: List<Object?>): Environment {
    val env = newEnclosedEnvironment(fn.env)
    for ((i, param) in fn.parameters.withIndex()) {
        env[param.value] = args[i]
    }
    return env
}

fun isTruthy(condition: Object?): kotlin.Boolean {
    return when (condition) {
        NULL -> false
        TRUE -> true
        FALSE -> false
        else -> true
    }
}

fun unwrapReturnValue(obj: Object?): Object? {
    return when (obj) {
        is ReturnValue -> obj.value
        else -> obj
    }
}

fun evalBlockStatement(block: BlockStatement, env: Environment): Object? {
    var result: Object? = null
    for (stmt in block.statements) {
        result = eval(stmt, env)
        result?.let {
            if (it.type() == ObjectType.RETURN_VALUE ||
                it.type() == ObjectType.ERROR
            ) {
                return result
            }
        }
    }
    return result
}

fun evalIdentifier(node: Identifier, env: Environment): Object? {
    env[node.value]?.let {
        return it
    }

    builtins[node.value]?.let {
        return it
    }

    return Error("identifier not found: ${node.value}")
}

fun evalIndexExpression(left: Object?, index: Object?): Object? {
    return when {
        left?.type() == ObjectType.ARRAY && index?.type() == ObjectType.INTEGER -> {
            evalArrayIndexExpression(left, index)
        }
        left?.type() == ObjectType.HASH -> {
            evalHashIndexExpression(left, index)
        }
        else -> Error("index operator not supported: ${left?.type()}")
    }
}

fun evalArrayIndexExpression(array: Object, index: Object): Object? {
    val arrayObject = array as MonkeyArray
    val idx = (index as Integer).value
    val max = arrayObject.elements.size - 1
    if (idx < 0 ||max < idx) {
        return NULL
    }

    return arrayObject.elements[idx.toInt()]
}

fun evalHashIndexExpression(hash: Object, index: Object?): Object? {
    val hashObject = hash as Hash
    val key = index as? Hashable ?: return Error("unusable as hash key: ${index?.type()}")
    val pair = hashObject.pairs[key.hashKey()] ?: return NULL
    return pair.value
}


fun evalHashLiteral(node: HashLiteral, env: Environment): Object? {
    val pairs = mutableMapOf<HashKey, HashPair>()

    for ((keyNode, valueNode) in node.pairs) {
        val key = eval(keyNode, env)
        if (key.isError()) {
            return key
        }

        val hashKey = key as? Hashable ?: return Error("unusable as hash key: ${key?.type()}")

        val value = eval(valueNode, env)
        if (value.isError()) {
            return value
        }
        if (value == null) {
            return Error("value is null")
        }

        val hashed = hashKey.hashKey()
        pairs[hashed] = HashPair(key, value)
    }

    return Hash(pairs)
}

