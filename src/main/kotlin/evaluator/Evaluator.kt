package evaluator

import monkey.`object`.Boolean
import monkey.`object`.Integer
import monkey.`object`.Null
import monkey.`object`.Object
import monkey.ast.Bool
import monkey.ast.ExpressionStatement
import monkey.ast.IntegerLiteral
import monkey.ast.Node
import monkey.ast.Program
import monkey.ast.Statement


val TRUE = Boolean(true)
val FALSE = Boolean(false)
val NULL = Null()

fun eval(node: Node?): Object? {
    return when (node) {
        is Program -> evalStatements(node.statements)
        is ExpressionStatement -> eval(node.value)
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

