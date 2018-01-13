package monkey.parser

import monkey.ast.Bool
import monkey.ast.Expression
import monkey.ast.ExpressionStatement
import monkey.ast.Identifier
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.LetStatement
import monkey.ast.PrefixExpression
import monkey.ast.ReturnStatement
import monkey.ast.Statement
import monkey.lexer.Lexer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
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

        assertThat(program.statements).hasSize(3)

        val tests = arrayOf("x", "y", "foobar")

        for ((i, test) in tests.withIndex()) {
            testLetStatement(program.statements[i], test)
        }
    }

    private fun testLetStatement(s: Statement, name: String) {
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

        assertThat(program.statements).hasSize(3)

        for (stmt in program.statements) {
            assertThat(stmt).isInstanceOf(ReturnStatement::class.java)
            val ret = stmt as ReturnStatement
            assertThat(ret.tokenLiteral()).isEqualTo("return")
        }
    }


    @Test
    fun identifierExpression() {
        val input = "foobar;"

        val l = Lexer.newInstance(input)
        val p = Parser.newInstance(l)
        val program = p.parseProgram()
        checkParserErrors(p)

        assertThat(program.statements).hasSize(1)

        val stmt = program.statements[0] as ExpressionStatement
        val ident = stmt.value as Identifier

        testIdentifier(ident, "foobar");
    }


    @Test
    fun integerLiteralExpression() {
        val input = "5;"

        val l = Lexer.newInstance(input)
        val p = Parser.newInstance(l)
        val program = p.parseProgram()
        checkParserErrors(p)

        assertThat(program.statements).hasSize(1)

        val stmt = program.statements[0] as ExpressionStatement
        val literal = stmt.value as IntegerLiteral

        assertThat(literal.value).isEqualTo(5)
        assertThat(literal.tokenLiteral()).isEqualTo("5")
    }

    @Test
    fun booleanExpression() {
        val prefixTests = arrayOf(
                "true" to true,
                "false" to false
        )

        for (test in prefixTests) {
            val p = Parser.newInstance(Lexer.newInstance(test.first))
            val program = p.parseProgram()
            checkParserErrors(p)
            assertThat(program.statements).hasSize(1)
            val stmt = program.statements[0] as ExpressionStatement
            val boolean = stmt.value as Bool
            assertThat(boolean.value).isEqualTo(test.second)
        }
    }

    @Test
    fun parsingPrefixExpressions() {
        val prefixTests = arrayOf(
                Triple("!5;", "!", 5L),
                Triple("-15;", "-", 15L),
                Triple("!true;", "!", true),
                Triple("!false;", "!", false)
        )

        for (test in prefixTests) {
            val p = Parser.newInstance(Lexer.newInstance(test.first))
            val program = p.parseProgram()
            checkParserErrors(p)
            assertThat(program.statements).hasSize(1)
            val stmt = program.statements[0] as ExpressionStatement
            val exp = stmt.value as PrefixExpression
            assertThat(exp.operator).isEqualTo(test.second)
            testLiteralExpression(exp.right, test.third)
        }
    }

    @Test
    fun parsingInfixExpressions() {
        data class TestData(val input: String,
                            val left: Any,
                            val operator: String,
                            val right: Any)

        val prefixTests = arrayOf(
                TestData("5 + 5;", 5, "+", 5),
                TestData("5 - 5;", 5, "-", 5),
                TestData("5 * 5;", 5, "*", 5),
                TestData("5 / 5;", 5, "/", 5),
                TestData("5 > 5;", 5, ">", 5),
                TestData("5 < 5;", 5, "<", 5),
                TestData("5 == 5;", 5, "==", 5),
                TestData("5 != 5;", 5, "!=", 5),
                TestData("true == true", true, "==", true),
                TestData("true != false", true, "!=", false),
                TestData("false == false", false, "==", false)
        )

        for (test in prefixTests) {
            val p = Parser.newInstance(Lexer.newInstance(test.input))
            val program = p.parseProgram()
            checkParserErrors(p)
            assertThat(program.statements).hasSize(1)
            val stmt = program.statements[0] as ExpressionStatement
            val exp = stmt.value as InfixExpression
            testLiteralExpression(exp.left, test.left)
            assertThat(exp.operator).isEqualTo(test.operator)
            testLiteralExpression(exp.right, test.right)
        }
    }

    @Test
    fun operatorPrecedenceParsing() {
        val prefixTests = arrayOf(
                "-a * b" to "((-a) * b)",
                "!-a" to "(!(-a))",
                "a + b + c" to "((a + b) + c)",
                "a + b - c" to "((a + b) - c)",
                "a * b * c" to "((a * b) * c)",
                "a * b / c" to "((a * b) / c)",
                "a + b / c" to "(a + (b / c))",
                "a + b * c + d / e - f" to "(((a + (b * c)) + (d / e)) - f)",
                "3 + 4; -5 * 5" to "(3 + 4)((-5) * 5)",
                "5 > 4 == 3 < 4" to "((5 > 4) == (3 < 4))",
                "5 < 4 != 3 > 4" to "((5 < 4) != (3 > 4))",
                "3 + 4 * 5 == 3 * 1 + 4 * 5" to "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
                "true" to "true",
                "false" to "false",
                "3 > 5 == false" to "((3 > 5) == false)",
                "3 < 5 == true" to "((3 < 5) == true)"
        )

        for (test in prefixTests) {
            val p = Parser.newInstance(Lexer.newInstance(test.first))
            val program = p.parseProgram()
            checkParserErrors(p)

            assertThat(program.string()).isEqualToIgnoringCase(test.second)
        }
    }

    fun testLiteralExpression(exp: Expression?, expected: Any) {
        when (expected) {
            is Int -> testIntegerLiteral(exp, expected.toLong())
            is Long -> testIntegerLiteral(exp, expected)
            is String -> testIdentifier(exp, expected)
            is Boolean -> testBoolLiteral(exp, expected)
            else -> Assert.fail("")
        }
    }

    fun testInfixExpression(exp: Expression, left: Any, operator: String, right: Any) {
        val opExp = exp as InfixExpression
        testLiteralExpression(opExp.left, left)
        assertThat(opExp.operator).isEqualTo(operator)
        testLiteralExpression(opExp.right, right)
    }

    private fun testIntegerLiteral(right: Expression?, value: Any) {
        val int = right as IntegerLiteral
        assertThat(int.value).isEqualTo(value)
        assertThat(int.tokenLiteral()).isEqualTo(value.toString())
    }

    private fun testIdentifier(right: Expression?, value: String) {
        val int = right as Identifier
        assertThat(int.value).isEqualTo(value)
        assertThat(int.tokenLiteral()).isEqualTo(value)
    }

    private fun testBoolLiteral(right: Expression?, value: Boolean) {
        val boolean = right as Bool
        assertThat(boolean.value).isEqualTo(value)
        assertThat(boolean.tokenLiteral()).isEqualTo(value.toString())
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
