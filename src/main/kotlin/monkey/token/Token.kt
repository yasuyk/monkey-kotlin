package monkey.token

const val ILLEGAL = "ILLEGAL"
const val EOF = "EOF"

// Identifiers + literals
const val IDENT = "IDENT" // add, foobar, x, y, ...
const val INT = "INT"   // 1343456

// Operators
const val ASSIGN = "="
const val PLUS = "+"
const val MINUS = "-"
const val BANG = "!"
const val ASTERISK = "*"
const val SLASH = "/"

const val LT = "<"
const val GT = ">"

const val EQ = "=="
const val NOT_EQ = "!="

// Delimiters
const val COMMA = ","
const val SEMICOLON = ";"

const val LPAREN = "("
const val RPAREN = ")"
const val LBRACE = "{"
const val RBRACE = "}"

// Keywords
const val FUNCTION = "FUNCTION"
const val LET = "LET"
const val TRUE = "TRUE"
const val FALSE = "FALSE"
const val IF = "IF"
const val ELSE = "ELSE"
const val RETURN = "RETURN"

typealias TokenType = String

data class Token(val type: TokenType, val literal: String)

val keywords = mapOf(
        "fn" to FUNCTION,
        "let" to LET,
        "true" to TRUE,
        "false" to FALSE,
        "if" to IF,
        "else" to ELSE,
        "return" to RETURN
)

fun String.lookupIdent(): TokenType = keywords[this] ?: IDENT
