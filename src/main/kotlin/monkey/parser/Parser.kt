package monkey.parser

import monkey.ast.Bool
import monkey.ast.Expression
import monkey.ast.ExpressionStatement
import monkey.ast.Identifier
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.LetStatement
import monkey.ast.PrefixExpression
import monkey.ast.Program
import monkey.ast.ReturnStatement
import monkey.ast.Statement
import monkey.lexer.Lexer
import monkey.token.ASSIGN
import monkey.token.ASTERISK
import monkey.token.BANG
import monkey.token.EOF
import monkey.token.EQ
import monkey.token.FALSE
import monkey.token.GT
import monkey.token.IDENT
import monkey.token.ILLEGAL
import monkey.token.INT
import monkey.token.LET
import monkey.token.LPAREN
import monkey.token.LT
import monkey.token.MINUS
import monkey.token.NOT_EQ
import monkey.token.PLUS
import monkey.token.RETURN
import monkey.token.RPAREN
import monkey.token.SEMICOLON
import monkey.token.SLASH
import monkey.token.TRUE
import monkey.token.Token
import monkey.token.TokenType

typealias prefixParseFn = () -> Expression?
typealias infixParseFn = (Expression?) -> Expression?

enum class Precedence {
    LOWEST,
    // ==
    EQUALS,
    // > or <
    LESS_GREATER,
    // +
    SUM,
    // *
    PRODUCT,
    // -X or !X
    PREFIX,
    // myFunction(X)
    CALL
}

val precedences = mapOf(
        EQ to Precedence.EQUALS,
        NOT_EQ to Precedence.EQUALS,
        LT to Precedence.LESS_GREATER,
        GT to Precedence.LESS_GREATER,
        PLUS to Precedence.SUM,
        MINUS to Precedence.SUM,
        SLASH to Precedence.PRODUCT,
        ASTERISK to Precedence.PRODUCT
)

class Parser private constructor(private val lexer: Lexer) {
    val errors = arrayListOf<String>()

    private lateinit var curToken: Token
    private var peekToken: Token = Token(ILLEGAL, "")

    private val prefixParseFns = mutableMapOf<TokenType, prefixParseFn>()
    private val infixParseFns = mutableMapOf<TokenType, infixParseFn>()

    companion object {
        fun newInstance(lexer: Lexer): Parser {
            return Parser(lexer).apply {
                // Read two tokens, so curToken and peekToken are both set
                nextToken()
                nextToken()

                registerPrefix(IDENT, ::parseIdentifier)
                registerPrefix(INT, ::parseIntegerLiteral)
                registerPrefix(TRUE, ::parseBool)
                registerPrefix(FALSE, ::parseBool)
                registerPrefix(BANG, ::parsePrefixExpression)
                registerPrefix(MINUS, ::parsePrefixExpression)
                registerPrefix(LPAREN, ::parseGroupedExpression)

                for (infix in arrayOf(PLUS, MINUS, SLASH, ASTERISK, EQ, NOT_EQ, LT, GT)) {
                    registerInfix(infix, ::parseInfixExpression)
                }
            }
        }
    }

    fun nextToken() {
        curToken = peekToken
        peekToken = lexer.nextToken()
    }

    fun parseProgram(): Program {
        val program = Program()
        while (true) {
            if (curToken.type == EOF) {
                break
            }
            parseStatement()?.let {
                program.statements.add(it)
            }
            nextToken()
        }
        return program
    }

    private fun parseStatement(): Statement? {
        return when (curToken.type) {
            LET -> parseLetStatement()
            RETURN -> parseReturnStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseLetStatement(): LetStatement? {
        val token = curToken

        if (!expectPeek(IDENT)) {
            return null
        }

        val identifier = Identifier(curToken, curToken.literal)

        if (!expectPeek(ASSIGN)) {
            return null
        }

        //TODO: We're skipping the expressions until we encounter a semicolon
        while (true) {
            if (curTokenIs(SEMICOLON)) {
                break
            }
            nextToken()
        }

        return LetStatement(token, identifier)
    }

    private fun parseReturnStatement(): ReturnStatement? {
        val stmt = ReturnStatement(curToken)
        nextToken()

        //TODO: We're skipping the expressions until we encounter a semicolon
        while (true) {
            if (curTokenIs(SEMICOLON)) {
                break
            }
            nextToken()
        }
        return stmt
    }

    private fun parseExpressionStatement(): ExpressionStatement? {
        val tok = curToken
        val expression = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(SEMICOLON)) {
            nextToken()
        }

        return ExpressionStatement(tok, expression)
    }

    private fun parseExpression(precedence: Precedence): Expression? {
        val prefix = prefixParseFns[curToken.type]
        if (prefix == null) {
            noPrefixParseFnError(curToken.type)
            return null
        }
        var leftExp = prefix()
        while (true) {
            if (!(!peekTokenIs(SEMICOLON) && precedence < peekPrecedence())) {
                break
            }
            val infix = infixParseFns[peekToken.type] ?: return leftExp
            nextToken()
            leftExp = infix(leftExp)
        }

        return leftExp
    }

    fun parseIdentifier(): Expression {
        return Identifier(curToken, curToken.literal)
    }

    fun parseIntegerLiteral(): Expression? {
        return try {
            IntegerLiteral(curToken, curToken.literal.toLong())
        } catch (e: NumberFormatException) {
            peekError("could not parse ${curToken.literal} as integer")
            null
        }
    }

    fun parseBool(): Expression {
        return Bool(curToken, curTokenIs(TRUE))
    }

    fun parsePrefixExpression(): Expression? {
        val token = curToken
        nextToken()
        val right = parseExpression(Precedence.PREFIX)
        return PrefixExpression(token, token.literal, right)
    }

    fun parseInfixExpression(left: Expression?): Expression? {
        val token = curToken
        val precedence = curPrecedence()
        nextToken()
        val right = parseExpression(precedence)
        return InfixExpression(token, left, token.literal, right)
    }

    fun parseGroupedExpression(): Expression? {
        nextToken()
        val exp = parseExpression(Precedence.LOWEST)
        if (!expectPeek(RPAREN)) {
            return null
        }
        return exp
    }

    private fun curTokenIs(t: TokenType): Boolean = curToken.type == t
    private fun peekTokenIs(t: TokenType): Boolean = peekToken.type == t

    private fun expectPeek(t: TokenType): Boolean {
        return if (peekTokenIs(t)) {
            nextToken()
            true
        } else {
            peekError(t)
            false
        }
    }

    private fun noPrefixParseFnError(t: TokenType) {
        errors.add("no prefix parse function for $t found")
    }

    private fun peekError(t: TokenType) {
        errors.add("expected next token to be $t, got ${peekToken.type} instead")
    }

    fun registerPrefix(type: TokenType, fn: prefixParseFn) {
        prefixParseFns[type] = fn
    }

    fun registerInfix(type: TokenType, fn: infixParseFn) {
        infixParseFns[type] = fn
    }

    private fun peekPrecedence(): Precedence = precedences[peekToken.type] ?: Precedence.LOWEST
    private fun curPrecedence(): Precedence = precedences[curToken.type] ?: Precedence.LOWEST

}


