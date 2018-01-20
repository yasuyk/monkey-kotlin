package monkey.evaluator

import monkey.`object`.Boolean
import monkey.`object`.Environment
import monkey.`object`.Error
import monkey.`object`.Function
import monkey.`object`.Hash
import monkey.`object`.Integer
import monkey.`object`.MonkeyArray
import monkey.`object`.MonkeyString
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
""" to 10,
            """
let f = fn(x) {
  return x;
  x + 10;
};
f(10);
""" to 10,
            """
let f = fn(x) {
   let result = x + 10;
   return result;
   return 10;
};
f(10);
""" to 20
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
            "\"Hello\" - \"World\"" to "unknown operator: STRING - STRING",
            "if (10 > 1) { true + false; }" to "unknown operator: BOOLEAN + BOOLEAN",
            """
if (10 > 1) {
  if (10 > 1) {
    return true + false;
  }

  return 1;
}
""" to "unknown operator: BOOLEAN + BOOLEAN",
            "foobar" to "identifier not found: foobar",
            """{"name": "Monkey"}[fn(x) { x }];""" to "unusable as hash key: FUNCTION",
            "999[1]" to "index operator not supported: INTEGER"
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

    @Test
    fun functionObject() {
        val input = "fn(x) { x + 2; };"
        val evaluated = testEval(input)
        val fn = evaluated as Function
        assertThat(fn.parameters).hasSize(1)
        assertThat(fn.parameters[0].string()).isEqualTo("x")
        assertThat(fn.body.string()).isEqualTo("(x + 2)")
    }

    @Test
    fun functionApplication() {
        val tests = arrayOf(
            "let identity = fn(x) { x; }; identity(5);" to 5,
            "let identity = fn(x) { return x; }; identity(5);" to 5,
            "let double = fn(x) { x * 2; }; double(5);" to 10,
            "let add = fn(x, y) { x + y; }; add(5, 5);" to 10,
            "let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));" to 20,
            "fn(x) { x; }(5)" to 5
        )
        for ((input, expected) in tests) {
            testIntegerObject(testEval(input), expected.toLong())
        }
    }

    @Test
    fun closures() {
        val input = """
            let newAdder = fn(x) {
              fn(y) { x + y };
            };
            let addTwo = newAdder(2);
            addTwo(2);
            """
        testIntegerObject(testEval(input), 4)
    }

    @Test
    fun stringLiteral() {
        val input = "\"Hello World!\""
        val evaluated = testEval(input)
        val str = evaluated as MonkeyString
        assertThat(str.value).isEqualTo("Hello World!")
    }

    @Test
    fun stringConcatenation() {
        val input = "\"Hello\" + \" \" + \"World!\""
        val evaluated = testEval(input)
        val str = evaluated as MonkeyString
        assertThat(str.value).isEqualTo("Hello World!")
    }

    @Test
    fun builtinFunctions() {
        val tests = arrayOf(
            """len("")""" to 0,
            """len("four")""" to 4,
            """len("hello world")""" to 11,
            """len(1)""" to "argument to `len` not supported, got INTEGER",
            """len("one", "two")""" to "wrong number of arguments. got=2, want=1"
        )
        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            when (expected) {
                is Int -> testIntegerObject(evaluated, expected.toLong())
                is String -> {
                    val err = evaluated as Error
                    assertThat(err.message).isEqualTo(expected)
                }
            }
        }
    }

    @Test
    fun arrayLiterals() {
        val input = "[1, 2 * 2, 3 + 3]"
        val evaluated = testEval(input)
        val result = evaluated as MonkeyArray
        assertThat(result.elements).hasSize(3)
        testIntegerObject(result.elements[0], 1)
        testIntegerObject(result.elements[1], 4)
        testIntegerObject(result.elements[2], 6)
    }

    @Test
    fun arrayIndexExpressions() {
        val tests = arrayOf(
            "[1, 2, 3][0]" to 1,
            "[1, 2, 3][1]" to 2,
            "[1, 2, 3][2]" to 3,
            "let i = 0; [1][i];" to 1,
            "[1, 2, 3][1 + 1];" to 3,
            "let myArray = [1, 2, 3]; myArray[2];" to 3,
            "let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];" to 6,
            "let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i]" to 2,
            "[1, 2, 3][3]" to NULL,
            "[1, 2, 3][-1]" to NULL
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            val integer = evaluated as? Integer
            if (integer != null && expected is Int) {
                testIntegerObject(integer, expected.toLong())
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun hashLiterals() {
        val input = """let two = "two";
{
    "one": 10 - 9,
    two: 1 + 1,
    "thr" + "ee": 6 / 2,
    4: 4,
    true: 5,
    false: 6
}
"""
        val evaluated = testEval(input)
        val result = evaluated as Hash
        val expected = mapOf(
            MonkeyString("one").hashKey() to 1,
            MonkeyString("two").hashKey() to 2,
            MonkeyString("three").hashKey() to 3,
            Integer(4).hashKey() to 4,
            TRUE.hashKey() to 5,
            FALSE.hashKey() to 6
        )

        assertThat(result.pairs).hasSize(expected.size)

        for ((expectedKey, expectedValue) in expected) {
            val pair = result.pairs[expectedKey]
            testIntegerObject(pair?.value, expectedValue.toLong())
        }

    }

    @Test
    fun hashIndexExpressions() {
        val tests = arrayOf(
            """{"foo": 5}["foo"]""" to 5,
            """{"foo": 5}["bar"]""" to null,
            """let key = "foo"; {"foo": 5}[key]""" to 5,
            """{}["foo"]""" to null,
            """{5: 5}[5]""" to 5,
            """{true: 5}[true]""" to 5,
            """{false: 5}[false]""" to 5
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            val integer = evaluated as? Integer
            if (integer != null && expected is Int) {
                testIntegerObject(integer, expected.toLong())
            } else {
                testNullObject(evaluated)
            }
        }
    }

    companion object {
        fun testEval(input: String): Object? {
            val p = Parser.newInstance(Lexer.newInstance(input)).parseProgram()
            val env = Environment()
            return eval(p, env)
        }

        fun testIntegerObject(obj: Object?, expected: Long) {
            val result = obj as Integer
            assertThat(result.value).isEqualTo(expected)
        }
    }


    private fun testBooleanObject(obj: Object?, expected: kotlin.Boolean) {
        val result = obj as Boolean
        assertThat(result.value).isEqualTo(expected)
    }

    private fun testNullObject(obj: Object?) {
        assertThat(obj).isEqualTo(NULL)
    }


}
