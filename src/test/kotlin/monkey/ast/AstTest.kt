package monkey.ast

import monkey.token.IDENT
import monkey.token.LET
import monkey.token.Token
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AstTest {

    @Test
    fun testString() {
        val statements = arrayListOf<Statement>(
            LetStatement(
                Token(LET, "let"),
                Identifier(Token(IDENT, "myVar"), "myVar"),
                Identifier(Token(IDENT, "anotherVar"), "anotherVar")
            )
        )

        assertThat(Program(statements).string()).isEqualTo("let myVar = anotherVar;")
    }

}
