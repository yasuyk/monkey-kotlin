package monkey.lexer

import monkey.token.ASSIGN
import monkey.token.ASTERISK
import monkey.token.BANG
import monkey.token.COMMA
import monkey.token.EOF
import monkey.token.EQ
import monkey.token.GT
import monkey.token.ILLEGAL
import monkey.token.INT
import monkey.token.LBRACE
import monkey.token.LPAREN
import monkey.token.LT
import monkey.token.MINUS
import monkey.token.NOT_EQ
import monkey.token.PLUS
import monkey.token.RBRACE
import monkey.token.RPAREN
import monkey.token.SEMICOLON
import monkey.token.SLASH
import monkey.token.STRING
import monkey.token.Token
import monkey.token.TokenType
import monkey.token.lookupIdent

class Lexer private constructor(private val input: String) {

    private var position = 0
    private var readPosition = 0
    private var ch: Char = 0.toChar()

    companion object {
        fun newInstance(input: String): Lexer {
            val l = Lexer(input)
            l.readChar()
            return l
        }
    }

    fun nextToken(): Token {
        skipWhitespace()

        val tok = when (ch) {
            '=' -> if (peekChar() == '=') {
                val ex = ch
                readChar()
                val literal = ex + ch.toString()
                Token(EQ, literal)
            } else {
                newToken(ASSIGN, ch)
            }
            '+' -> newToken(PLUS, ch)
            '-' -> newToken(MINUS, ch)
            '!' -> if (peekChar() == '=') {
                val ex = ch
                readChar()
                val literal = ex + ch.toString()
                Token(NOT_EQ, literal)
            } else {
                newToken(BANG, ch)
            }
            '/' -> newToken(SLASH, ch)
            '*' -> newToken(ASTERISK, ch)
            '<' -> newToken(LT, ch)
            '>' -> newToken(GT, ch)
            ';' -> newToken(SEMICOLON, ch)
            ',' -> newToken(COMMA, ch)
            '{' -> newToken(LBRACE, ch)
            '}' -> newToken(RBRACE, ch)
            '(' -> newToken(LPAREN, ch)
            ')' -> newToken(RPAREN, ch)
            '"' -> Token(STRING, readString())
            0.toChar() -> Token(EOF, "")
            else -> when {
                isLetter(ch) -> {
                    val literal = readIdentifier()
                    return Token(literal.lookupIdent(), literal)
                }
                isDigit(ch) -> return Token(INT, readNumber())
                else -> newToken(ILLEGAL, ch)
            }
        }

        readChar()
        return tok
    }

    fun readChar() {
        ch = if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
        position = readPosition
        readPosition += 1
    }

    private fun peekChar(): Char {
        return if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
    }

    private fun readIdentifier(): String {
        val pos = position
        while (isLetter(ch)) {
            readChar()
        }
        return input.substring(pos until position)
    }

    private fun readNumber(): String {
        val pos = position
        while (isDigit(ch)) {
            readChar()
        }
        return input.substring(pos until position)
    }

    private fun readString(): String {
        val pos = position + 1
        while (true) {
            readChar()
            if (ch == '"' || ch == 0.toChar()) {
                break
            }
        }
        return input.substring(pos until position)
    }


    private fun skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar()
        }

    }

    private fun isLetter(ch: Char): Boolean {
        return ch in 'a'..'z' || ch in 'A'..'Z' || ch == '_'
    }

    private fun isDigit(ch: Char): Boolean {
        return ch in '0'..'9'
    }

    private fun newToken(tokenType: TokenType, ch: Char) = Token(tokenType, ch.toString())

}
