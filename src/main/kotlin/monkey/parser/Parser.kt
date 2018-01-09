package monkey.parser

import monkey.ast.Identifier
import monkey.ast.LetStatement
import monkey.ast.Program
import monkey.ast.ReturnStatement
import monkey.ast.Statement
import monkey.lexer.Lexer
import monkey.token.ASSIGN
import monkey.token.EOF
import monkey.token.IDENT
import monkey.token.ILLEGAL
import monkey.token.LET
import monkey.token.RETURN
import monkey.token.SEMICOLON
import monkey.token.Token
import monkey.token.TokenType

class Parser private constructor(private val lexer: Lexer) {
    val errors = arrayListOf<String>()

    private lateinit var curToken: Token
    private var peekToken: Token = Token(ILLEGAL, "")

    companion object {
        fun newInstance(lexer: Lexer): Parser {
            return Parser(lexer).apply {
                // Read two tokens, so curToken and peekToken are both set
                nextToken()
                nextToken()
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
            else -> null
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

    private fun peekError(t: TokenType) {
        errors.add("expected next token to be $t, got ${peekToken.type} instead")
    }
}
