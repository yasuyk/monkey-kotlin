package monkey.ast

import monkey.token.Token

interface Node {
    fun tokenLiteral(): String
}

interface Statement : Node {
    fun statementNode()
}

interface Expression : Node {
    fun expressionNode()
}

class Program {
    val statements = arrayListOf<Statement>()

    fun tolenLiteral(): String {
        return if (statements.size > 0) {
            statements[0].tokenLiteral()
        } else {
            ""
        }
    }
}


class LetStatement(private val token: Token, val name: Identifier) : Statement {
    lateinit var value: Expression

    override fun tokenLiteral() = token.literal
    override fun statementNode() {}
}

class ReturnStatement(private val token: Token) : Statement {
    lateinit var value: Expression
    override fun tokenLiteral() = token.literal
    override fun statementNode() {}
}

class Identifier(private val token: Token, val value: String) : Expression {
    override fun tokenLiteral() = token.literal
    override fun expressionNode() {}
}
