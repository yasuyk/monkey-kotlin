package evaluator

import monkey.`object`.Boolean
import monkey.`object`.Environment
import monkey.`object`.Error
import monkey.`object`.Integer
import monkey.`object`.Object
import monkey.lexer.Lexer
import monkey.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatorTest {

    @Test
    fun evalIntegerExpression() {
        val tests = arrayOf(
                "5" to 5,
                "10" to 10,
                "-5" to -5,
                "-10" to -10,
                "5 + 5 + 5 + 5 - 10" to 10,
                "2 * 2 * 2 * 2 * 2" to 32,
                "-50 + 100 + -50" to 0,
                "5 * 2 + 10" to 20,
                "5 + 2 * 10" to 25,
                "20 + 2 * -10" to 0,
                "50 / 2 * 2 + 10" to 60,
                "2 * (5 + 10)" to 30,
                "3 * 3 * 3 + 10" to 37,
                "3 * (3 * 3) + 10" to 37,
                "(5 + 10 * 2 + 15 / 3) * 2 + -10" to 50
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testIntegerObject(evaluated, expected.toLong())
        }
    }

    @Test
    fun evalBooleanExpression() {
        val tests = arrayOf(
                "true" to true,
                "false" to false
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testBooleanObject(evaluated, expected)
        }
    }

    @Test
    fun ifElseExpression() {
        val tests = arrayOf(
                "if (true) { 10 }" to 10,
                "if (false) { 10 " to null,
                "if (1) { 10 }" to 10,
                "if (1 < 2) { 10 }" to 10,
                "if (1 > 2) { 10 }" to null,
                "if (1 > 2) { 10 } else { 20 }" to 20,
                "if (1 < 2) { 10 } else { 20 }" to 10
        )
        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            if (expected != null) {
                testIntegerObject(evaluated, expected.toLong())
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun evalBangOperator() {
        val tests = arrayOf(
                "!true" to false,
                "!false" to true,
                "!5" to false,
                "!!true" to true,
                "!!false" to false,
                "!!5" to true,
                "1 < 2" to true,
                "1 > 2" to false,
                "1 < 1" to false,
                "1 > 1" to false,
                "1 == 1" to true,
                "1 != 1" to false,
                "1 == 2" to false,
                "1 != 2" to true,
                "true == true" to true,
                "false == false" to true,
                "true == false" to false,
                "true != false" to true,
                "false != true" to true,
                "(1 < 2) == true" to true,
                "(1 < 2) == false" to false,
                "(1 > 2) == true" to false,
                "(1 > 2) == false" to true
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testBooleanObject(evaluated, expected)
        }
    }

    @Test
    fun returnStatements() {
        val tests = arrayOf(
                "return 10;" to 10,
                "return 10; 9;" to 10,
                "return 2 * 5; 9;" to 10,
                "9; return 2 * 5; 9;" to 10,
                "if (10 > 1) { return 10; }" to 10,
                """
if (10 > 1) {
  if (10 > 1) {
    return 10;
  }

  return 1;
}
""" to 10
        )
        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testIntegerObject(evaluated, expected.toLong())
        }
    }

    @Test
    fun errorHandling() {
        val tests = arrayOf(
                "5 + true;" to "type mismatch: INTEGER + BOOLEAN",
                "5 + true; 5;" to "type mismatch: INTEGER + BOOLEAN",
                "-true" to "unknown operator: -BOOLEAN",
                "true + false;" to "unknown operator: BOOLEAN + BOOLEAN",
                "5; true + false; 5" to "unknown operator: BOOLEAN + BOOLEAN",
                "if (10 > 1) { true + false; }" to "unknown operator: BOOLEAN + BOOLEAN",
                """
if (10 > 1) {
  if (10 > 1) {
    return true + false;
  }

  return 1;
}
""" to "unknown operator: BOOLEAN + BOOLEAN",
                "foobar" to "identifier not found: foobar"
        )
        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            val err = evaluated as Error
            assertThat(err.message).isEqualTo(expected)
        }
    }

    @Test
    fun retStatements() {
        val tests = arrayOf(
                "let a = 5; a;" to 5,
                "let a = 5 * 5; a;" to 25,
                "let a = 5; let b = a; b;" to 5,
                "let a = 5; let b = a; let c = a + b + 5; c;" to 15
        )
        for ((input, expected) in tests) {
            testIntegerObject(testEval(input), expected.toLong())
        }
    }

    private fun testEval(input: String): Object? {
        val p = Parser.newInstance(Lexer.newInstance(input)).parseProgram()
        val env = Environment()
        return eval(p, env)
    }


    private fun testIntegerObject(obj: Object?, expected: Long) {
        val result = obj as Integer
        assertThat(result.value).isEqualTo(expected)
    }

    private fun testBooleanObject(obj: Object?, expected: kotlin.Boolean) {
        val result = obj as Boolean
        assertThat(result.value).isEqualTo(expected)
    }

    private fun testNullObject(obj: Object?) {
        assertThat(obj).isEqualTo(NULL)
    }


}
