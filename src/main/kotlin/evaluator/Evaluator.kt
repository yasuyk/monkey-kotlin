package evaluator

import monkey.`object`.Boolean
import monkey.`object`.Environment
import monkey.`object`.Environment.Companion.newEnclosedEnvironment
import monkey.`object`.Error
import monkey.`object`.Function
import monkey.`object`.Integer
import monkey.`object`.Null
import monkey.`object`.Object
import monkey.`object`.ObjectType
import monkey.`object`.ReturnValue
import monkey.`object`.isError
import monkey.ast.BlockStatement
import monkey.ast.Bool
import monkey.ast.CallExpression
import monkey.ast.Expression
import monkey.ast.ExpressionStatement
import monkey.ast.FunctionLiteral
import monkey.ast.Identifier
import monkey.ast.IfExpression
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.LetStatement
import monkey.ast.Node
import monkey.ast.PrefixExpression
import monkey.ast.Program
import monkey.ast.ReturnStatement


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
        is Identifier -> evalIdentifier(node, env)
        is IntegerLiteral -> Integer(node.value)
        is FunctionLiteral -> Function(node.parameters, node.body, env)
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
    if (left?.type() == ObjectType.INTEGER && right?.type() == ObjectType.INTEGER) {
        return evalIntegerInfixExpression(operator, left, right)
    }
    return when {
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
    val function = fn as? Function ?: return Error("not a function: ${fn?.type()}")
    val extendedEnv = extendFunctionEnv(function, args)
    val evaluated = eval(function.body, extendedEnv)
    return unwrapReturnValue(evaluated)
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
    return when(obj) {
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
                    it.type() == ObjectType.ERROR) {
                return result
            }
        }
    }
    return result
}

fun evalIdentifier(node: Identifier, env: Environment): Object? {
    return env[node.value] ?: Error("identifier not found: ${node.value}")
}
