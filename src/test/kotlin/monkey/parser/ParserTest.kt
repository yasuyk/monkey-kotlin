package monkey.parser

import monkey.ast.Bool
import monkey.ast.CallExpression
import monkey.ast.Expression
import monkey.ast.ExpressionStatement
import monkey.ast.FunctionLiteral
import monkey.ast.Identifier
import monkey.ast.IfExpression
import monkey.ast.InfixExpression
import monkey.ast.IntegerLiteral
import monkey.ast.LetStatement
import monkey.ast.PrefixExpression
import monkey.ast.ReturnStatement
import monkey.ast.Statement
import monkey.ast.StringLiteral
import monkey.lexer.Lexer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test

class ParserTest {

    @Test
    fun letStatements() {
        val tests = arrayOf(
            Triple("let x = 5;", "x", 5),
            Triple("let y = true;", "y", true),
            Triple("let foobar = y;", "foobar", "y")
        )

        for ((input, expectedIdentifier, expectedValue) in tests) {
            val l = Lexer.newInstance(input)
            val p = Parser.newInstance(l)
            val program = p.parseProgram()
            checkParserErrors(p)
            assertThat(program.statements).hasSize(1)
            val stmt = program.statements[0]
            testLetStatement(stmt, expectedIdentifier)
            val let = stmt as LetStatement
            testLiteralExpression(let.value, expectedValue)
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
        val tests = arrayOf(
            "return 5;" to 5,
            "return true;" to true,
            "return foobar;" to "foobar"
        )
        for ((input, expected) in tests) {
            val l = Lexer.newInstance(input)
            val p = Parser.newInstance(l)
            val program = p.parseProgram()
            checkParserErrors(p)
            assertThat(program.statements).hasSize(1)
            val stmt = program.statements[0]
            val ret = stmt as ReturnStatement
            assertThat(ret.tokenLiteral()).isEqualTo("return")
            testLiteralExpression(ret.value, expected)
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

        testIdentifier(ident, "foobar")
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
    fun stringLiteralExpression() {
        val input = "\"hello world\";"

        val l = Lexer.newInstance(input)
        val p = Parser.newInstance(l)
        val program = p.parseProgram()
        checkParserErrors(p)

        assertThat(program.statements).hasSize(1)

        val stmt = program.statements[0] as ExpressionStatement
        val literal = stmt.value as StringLiteral

        assertThat(literal.value).isEqualTo("hello world")
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
    fun ifExpression() {
        val p = Parser.newInstance(Lexer.newInstance("if (x < y) { x }"))
        val program = p.parseProgram()
        checkParserErrors(p)
        assertThat(program.statements).hasSize(1)
        val stmt = program.statements[0] as ExpressionStatement
        val exp = stmt.value as IfExpression
        testInfixExpression(exp.condition, "x", "<", "y")
        assertThat(exp.consequence.statements).hasSize(1)
        val consequence = exp.consequence.statements[0] as ExpressionStatement
        testIdentifier(consequence.value, "x")
        assertThat(exp.alternative).isNull()
    }

    @Test
    fun ifElseExpression() {
        val p = Parser.newInstance(Lexer.newInstance("if (x < y) { x } else { y }"))
        val program = p.parseProgram()
        checkParserErrors(p)
        assertThat(program.statements).hasSize(1)
        val stmt = program.statements[0] as ExpressionStatement
        val exp = stmt.value as IfExpression
        assertThat(exp.consequence.statements).hasSize(1)
        testInfixExpression(exp.condition, "x", "<", "y")
        val consequence = exp.consequence.statements[0] as ExpressionStatement
        testIdentifier(consequence.value, "x")
        assertThat(exp.alternative!!.statements).hasSize(1)
        val alternative = exp.alternative!!.statements[0] as ExpressionStatement
        testIdentifier(alternative.value, "y")
    }

    @Test
    fun functionLiteralParsing() {
        val input = "fn(x, y) { x + y; }"

        val p = Parser.newInstance(Lexer.newInstance(input))
        val program = p.parseProgram()
        checkParserErrors(p)
        assertThat(program.statements).hasSize(1)
        val stmt = program.statements[0] as ExpressionStatement
        val function = stmt.value as FunctionLiteral
        assertThat(function.parameters).hasSize(2)
        testLiteralExpression(function.parameters[0], "x")
        testLiteralExpression(function.parameters[1], "y")
        assertThat(function.body.statements).hasSize(1)
        val bodyStmt = function.body.statements[0] as ExpressionStatement
        testInfixExpression(bodyStmt.value!!, "x", "+", "y")
    }

    @Test
    fun functionParameterParsing() {
        val tests = arrayOf(
            "fn() {};" to arrayOf(),
            "fn(x) {};" to arrayOf("x"),
            "fn(x, y, z) {};" to arrayOf("x", "y", "z")
        )

        for ((input, expectedParams) in tests) {
            val p = Parser.newInstance(Lexer.newInstance(input))
            val program = p.parseProgram()
            checkParserErrors(p)
            val stmt = program.statements[0] as ExpressionStatement
            val function = stmt.value as FunctionLiteral
            assertThat(function.parameters).hasSize(expectedParams.size)

            for ((i, ident) in expectedParams.withIndex()) {
                testLiteralExpression(function.parameters[i], ident)
            }
        }
    }

    @Test
    fun callExpressionParsing() {
        val input = "add(1, 2 * 3, 4 + 5)"

        val p = Parser.newInstance(Lexer.newInstance(input))
        val program = p.parseProgram()
        checkParserErrors(p)
        assertThat(program.statements).hasSize(1)
        val stmt = program.statements[0] as ExpressionStatement
        val exp = stmt.value as CallExpression
        testIdentifier(exp.function, "add")
        assertThat(exp.arguments).hasSize(3)
        testLiteralExpression(exp.arguments[0], 1)
        testInfixExpression(exp.arguments[1], 2, "*", 3)
        testInfixExpression(exp.arguments[2], 4, "+", 5)
    }

    @Test
    fun callExpressionParameterParsing() {
        val tests = arrayOf(
            Triple("add();", "add", listOf()),
            Triple("add(1);", "add", listOf("1")),
            Triple("add(1, 2 * 3, 4 + 5);", "add", listOf("1", "(2 * 3)", "(4 + 5)"))
        )

        for ((input, expectdIdent, expectedArgs) in tests) {
            val p = Parser.newInstance(Lexer.newInstance(input))
            val program = p.parseProgram()
            checkParserErrors(p)
            val stmt = program.statements[0] as ExpressionStatement
            val exp = stmt.value as CallExpression
            testIdentifier(exp.function, expectdIdent)
            assertThat(exp.arguments.size).isEqualTo(expectedArgs.size)
            for ((i, arg) in expectedArgs.withIndex()) {
                assertThat(exp.arguments[i].string()).isEqualTo(arg)
            }
        }
    }


    @Test
    fun parsingPrefixExpressions() {
        val prefixTests = arrayOf(
            Triple("!5;", "!", 5L),
            Triple("-15;", "-", 15L),
            Triple("!foobar;", "!", "foobar"),
            Triple("-foobar;", "-", "foobar"),
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
        data class TestData(
            val input: String,
            val left: Any,
            val operator: String,
            val right: Any
        )

        val prefixTests = arrayOf(
            TestData("5 + 5;", 5, "+", 5),
            TestData("5 - 5;", 5, "-", 5),
            TestData("5 * 5;", 5, "*", 5),
            TestData("5 / 5;", 5, "/", 5),
            TestData("5 > 5;", 5, ">", 5),
            TestData("5 < 5;", 5, "<", 5),
            TestData("5 == 5;", 5, "==", 5),
            TestData("5 != 5;", 5, "!=", 5),
            TestData("foobar + barfoo;", "foobar", "+", "barfoo"),
            TestData("foobar - barfoo;", "foobar", "-", "barfoo"),
            TestData("foobar * barfoo;", "foobar", "*", "barfoo"),
            TestData("foobar / barfoo;", "foobar", "/", "barfoo"),
            TestData("foobar > barfoo;", "foobar", ">", "barfoo"),
            TestData("foobar < barfoo;", "foobar", "<", "barfoo"),
            TestData("foobar == barfoo;", "foobar", "==", "barfoo"),
            TestData("foobar != barfoo;", "foobar", "!=", "barfoo"),
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
            "3 < 5 == true" to "((3 < 5) == true)",
            "1 + (2 + 3) + 4" to "((1 + (2 + 3)) + 4)",
            "(5 + 5) * 2" to "((5 + 5) * 2)",
            "2 / (5 + 5)" to "(2 / (5 + 5))",
            "(5 + 5) * 2 * (5 + 5)" to "(((5 + 5) * 2) * (5 + 5))",
            "-(5 + 5)" to "(-(5 + 5))",
            "!(true == true)" to "(!(true == true))",
            "a + add(b * c) + d" to "((a + add((b * c))) + d)",
            "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))" to "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))",
            "add(a + b + c * d / f + g)" to "add((((a + b) + ((c * d) / f)) + g))"
        )

        for (test in prefixTests) {
            val p = Parser.newInstance(Lexer.newInstance(test.first))
            val program = p.parseProgram()
            checkParserErrors(p)

            assertThat(program.string()).isEqualToIgnoringCase(test.second)
        }
    }

    private fun testLiteralExpression(exp: Expression?, expected: Any) {
        when (expected) {
            is Int -> testIntegerLiteral(exp, expected.toLong())
            is Long -> testIntegerLiteral(exp, expected)
            is String -> testIdentifier(exp, expected)
            is Boolean -> testBoolLiteral(exp, expected)
            else -> Assert.fail("")
        }
    }

    private fun testInfixExpression(exp: Expression, left: Any, operator: String, right: Any) {
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
