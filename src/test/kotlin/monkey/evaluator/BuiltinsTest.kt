package monkey.evaluator

import monkey.`object`.Integer
import monkey.`object`.MonkeyArray
import monkey.evaluator.EvaluatorTest.Companion.testEval
import monkey.evaluator.EvaluatorTest.Companion.testIntegerObject
import org.assertj.core.api.Assertions
import org.junit.Test

class BuiltinsTest {

    @Test
    fun map() {
        val input = """
let map = fn(arr, f) {
  let iter = fn(arr, accumulated) {
    if (len(arr) == 0) {
      accumulated
    } else {
      iter(rest(arr), push(accumulated, f(first(arr))));
    }
  };

  iter(arr, []);
};

let a = [1, 2, 3, 4];
let double = fn(x) { x * 2 };
map(a, double);
"""
        val evaluated = testEval(input)
        val result = evaluated as MonkeyArray
        Assertions.assertThat(result.elements).hasSize(4)
        testIntegerObject(result.elements[0], 2)
        testIntegerObject(result.elements[1], 4)
        testIntegerObject(result.elements[2], 6)
        testIntegerObject(result.elements[3], 8)

    }

    @Test
    fun reduce() {
        val input = """
let reduce = fn(arr, initial, f) {
  let iter = fn(arr, result) {
    if (len(arr) == 0) {
      result
    } else {
      iter(rest(arr), f(result, first(arr)));
    }
  };

  iter(arr, initial);
};

let sum = fn(arr) {
  reduce(arr, 0, fn(initial, el) { initial + el });
};

sum([1, 2, 3, 4, 5]);
"""
        val evaluated = testEval(input)
        val result = evaluated as Integer
        Assertions.assertThat(result.value).isEqualTo(15L)
    }
}
