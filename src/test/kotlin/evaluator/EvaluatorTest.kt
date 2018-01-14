package evaluator

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
                "5" to 5L,
                "10" to 10L
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testIntegerObject(evaluated, expected)
        }
    }

    private fun testEval(input: String)
            = eval(Parser.newInstance(Lexer.newInstance(input)).parseProgram())

    private fun testIntegerObject(obj: Object?, expected: Long) {
        val result = obj as Integer
        assertThat(result.value).isEqualTo(expected)
    }

}
