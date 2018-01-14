package evaluator

import monkey.`object`.Boolean
import monkey.`object`.Integer
import monkey.`object`.Null
import monkey.`object`.Object
import monkey.`object`.ObjectType
import monkey.ast.Bool
import monkey.ast.ExpressionStatement
import monkey.ast.IntegerLiteral
import monkey.ast.Node
import monkey.ast.PrefixExpression
import monkey.ast.Program
import monkey.ast.Statement


val TRUE = Boolean(true)
val FALSE = Boolean(false)
val NULL = Null()

fun eval(node: Node?): Object? {
    return when (node) {
        is Program -> evalStatements(node.statements)
        is ExpressionStatement -> eval(node.value)
        is PrefixExpression -> evalPrefixExpression(node.operator, eval(node.right))
        is IntegerLiteral -> Integer(node.value)
        is Bool -> nativeBoolToBooleanObject(node.value)
        else -> null
    }
}

private fun evalStatements(statements: List<Statement>): Object? {
    var result: Object? = null
    for (stmt in statements) {
        result = eval(stmt)
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

