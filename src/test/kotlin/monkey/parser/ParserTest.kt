package monkey.parser

import monkey.ast.LetStatement
import monkey.ast.ReturnStatement
import monkey.ast.Statement
import monkey.lexer.Lexer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ParserTest {

    @Test
    fun letStatements() {
        val input = """
let x = 5;
let y = 10;
let foobar = 838383;
"""
        val l = Lexer.newInstance(input)
        val p = Parser.newInstance(l)
        val program = p.parseProgram()
        checkParserErrors(p)

        assertThat(program).isNotNull()
        assertThat(program!!.statements).hasSize(3)

        val tests = arrayOf("x", "y", "foobar")

        for ((i, test) in tests.withIndex()) {
            testLetStatement(program.statements[i], test)
        }
    }

    private fun testLetStatement(s: Statement, name : String) {
        assertThat(s.tokenLiteral()).isEqualTo("let")
        assertThat(s).isInstanceOf(LetStatement::class.java)
        val let = s as LetStatement
        assertThat(let.name.value).isEqualTo(name)
        assertThat(let.name.tokenLiteral()).isEqualTo(name)
    }

    @Test
    fun returnStatements() {
        val input = """
return 5;
return 10;
return 838383;
"""
        val l = Lexer.newInstance(input)
        val p = Parser.newInstance(l)
        val program = p.parseProgram()
        checkParserErrors(p)

        assertThat(program).isNotNull()
        assertThat(program!!.statements).hasSize(3)

        for (stmt in program.statements) {
            assertThat(stmt).isInstanceOf(ReturnStatement::class.java)
            val ret = stmt as ReturnStatement
            assertThat(ret.tokenLiteral()).isEqualTo("return")
        }
    }


    private fun checkParserErrors(p: Parser) {
        if (p.errors.isEmpty()) {
            return
        }

        println("parser has ${p.errors.size} errors")
        for (error in p.errors) {
            println("parser error: $error")
        }
        assert(false)
    }

}
