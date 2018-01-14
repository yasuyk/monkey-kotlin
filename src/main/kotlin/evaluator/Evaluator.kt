package evaluator

import monkey.`object`.Integer
import monkey.`object`.Object
import monkey.ast.ExpressionStatement
import monkey.ast.IntegerLiteral
import monkey.ast.Node
import monkey.ast.Program
import monkey.ast.Statement

fun eval(node: Node?): Object? {
    return when (node) {
        is Program -> evalStatements(node.statements)
        is ExpressionStatement -> eval(node.value)
        is IntegerLiteral -> Integer(node.value)
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
