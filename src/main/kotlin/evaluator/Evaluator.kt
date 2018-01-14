package evaluator

import monkey.`object`.Boolean
import monkey.`object`.Integer
import monkey.`object`.Null
import monkey.`object`.Object
import monkey.`object`.ObjectType
import monkey.`object`.ReturnValue
import monkey.ast.BlockStatement
import monkey.ast.Bool
import monkey.ast.ExpressionStatement
import monkey.ast.IfExpression
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.Node
import monkey.ast.PrefixExpression
import monkey.ast.Program
import monkey.ast.ReturnStatement


val TRUE = Boolean(true)
val FALSE = Boolean(false)
val NULL = Null()

fun eval(node: Node?): Object? {
    return when (node) {
        is Program -> evalProgram(node)
        is ExpressionStatement -> eval(node.value)
        is BlockStatement -> evalBlockStatement(node)
        is ReturnStatement -> ReturnValue(eval(node.value))
        is PrefixExpression -> evalPrefixExpression(node.operator, eval(node.right))
        is InfixExpression -> {
            evalInfixExpression(node.operator, eval(node.left), eval(node.right))
        }
        is IfExpression -> evalIfExpression(node)
        is IntegerLiteral -> Integer(node.value)
        is Bool -> nativeBoolToBooleanObject(node.value)
        else -> null
    }
}


private fun evalProgram(program : Program): Object? {
    var result: Object? = null
    for (stmt in program.statements) {
        result = eval(stmt)
        if (result is ReturnValue) {
            return result.value
        }
    }
    return result
}

fun nativeBoolToBooleanObject(value: kotlin.Boolean) = if (value) TRUE else FALSE

fun evalPrefixExpression(operator: String, right: Object?): Object? {
    return when (operator) {
        "!" -> evalBangOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> NULL
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
        return NULL
    }

    val int = right as Integer
    return Integer(-int.value)
}

fun evalInfixExpression(operator: String, left: Object?, right: Object?): Object? {
    if (left?.type() == ObjectType.INTEGER && right?.type() == ObjectType.INTEGER) {
        return evalIntegerInfixExpression(operator, left, right)
    }
    return when (operator) {
        "==" -> nativeBoolToBooleanObject(left == right)
        "!=" -> nativeBoolToBooleanObject(left != right)
        else -> NULL
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
        else -> NULL
    }
}

fun evalIfExpression(ie: IfExpression): Object? {
    val condition = eval(ie.condition)
    return when {
        isTruthy(condition) -> eval(ie.consequence)
        ie.alternative != null -> eval(ie.alternative)
        else -> NULL
    }
}

fun isTruthy(condition: Object?): kotlin.Boolean {
    return when (condition) {
        NULL -> false
        TRUE -> true
        FALSE -> false
        else -> true
    }
}

fun evalBlockStatement(block: BlockStatement): Object? {
    var result: Object? = null
    for (stmt in block.statements) {
        result = eval(stmt)
        if (result?.type() == ObjectType.RETURN_VALUE) {
            return result
        }
    }
    return result

}
