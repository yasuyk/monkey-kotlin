package monkey.lexer

import monkey.token.ASSIGN
import monkey.token.ASTERISK
import monkey.token.BANG
import monkey.token.COLON
import monkey.token.COMMA
import monkey.token.ELSE
import monkey.token.EOF
import monkey.token.EQ
import monkey.token.FALSE
import monkey.token.FUNCTION
import monkey.token.GT
import monkey.token.IDENT
import monkey.token.IF
import monkey.token.INT
import monkey.token.LBRACE
import monkey.token.LBRACKET
import monkey.token.LET
import monkey.token.LPAREN
import monkey.token.LT
import monkey.token.MINUS
import monkey.token.NOT_EQ
import monkey.token.PLUS
import monkey.token.RBRACE
import monkey.token.RBRACKET
import monkey.token.RETURN
import monkey.token.RPAREN
import monkey.token.SEMICOLON
import monkey.token.SLASH
import monkey.token.STRING
import monkey.token.TRUE
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class LexerTest {

    @Before
    fun setUp() {
    }

    @Test
    fun nextToken() {
        val input = """let five = 5;
let ten = 10;

let add = fn(x, y) {
  x + y;
};

let result = add(five, ten);
!-/*5;
5 < 10 > 5;

if (5 < 10) {
	return true;
} else {
	return false;
}

10 == 10;
10 != 9;
"foobar"
"foo bar"
[1, 2];
{"foo": "bar"}
"""

        val tests = arrayOf(
            LET to "let",
            IDENT to "five",
            ASSIGN to "=",
            INT to "5",
            SEMICOLON to ";",
            LET to "let",
            IDENT to "ten",
            ASSIGN to "=",
            INT to "10",
            SEMICOLON to ";",
            LET to "let",
            IDENT to "add",
            ASSIGN to "=",
            FUNCTION to "fn",
            LPAREN to "(",
            IDENT to "x",
            COMMA to ",",
            IDENT to "y",
            RPAREN to ")",
            LBRACE to "{",
            IDENT to "x",
            PLUS to "+",
            IDENT to "y",
            SEMICOLON to ";",
            RBRACE to "}",
            SEMICOLON to ";",
            LET to "let",
            IDENT to "result",
            ASSIGN to "=",
            IDENT to "add",
            LPAREN to "(",
            IDENT to "five",
            COMMA to ",",
            IDENT to "ten",
            RPAREN to ")",
            SEMICOLON to ";",
            BANG to "!",
            MINUS to "-",
            SLASH to "/",
            ASTERISK to "*",
            INT to "5",
            SEMICOLON to ";",
            INT to "5",
            LT to "<",
            INT to "10",
            GT to ">",
            INT to "5",
            SEMICOLON to ";",
            IF to "if",
            LPAREN to "(",
            INT to "5",
            LT to "<",
            INT to "10",
            RPAREN to ")",
            LBRACE to "{",
            RETURN to "return",
            TRUE to "true",
            SEMICOLON to ";",
            RBRACE to "}",
            ELSE to "else",
            LBRACE to "{",
            RETURN to "return",
            FALSE to "false",
            SEMICOLON to ";",
            RBRACE to "}",
            INT to "10",
            EQ to "==",
            INT to "10",
            SEMICOLON to ";",
            INT to "10",
            NOT_EQ to "!=",
            INT to "9",
            SEMICOLON to ";",
            STRING to "foobar",
            STRING to "foo bar",
            LBRACKET to "[",
            INT to "1",
            COMMA to ",",
            INT to "2",
            RBRACKET to "]",
            SEMICOLON to ";",
            LBRACE to "{",
            STRING to "foo",
            COLON to ":",
            STRING to "bar",
            RBRACE to "}",
            EOF to ""
        )

        val l = Lexer.newInstance(input)
        for (test in tests) {
            val tok = l.nextToken()
            assertThat(tok.type).isEqualTo(test.first)
            assertThat(tok.literal).isEqualTo(test.second)
        }
    }
}
