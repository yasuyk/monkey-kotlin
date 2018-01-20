package monkey.`object`

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ObjectTest {

    @Test
    fun stringHashKey() {
        val hello1 = MonkeyString("Hello World")
        val hello2 = MonkeyString("Hello World")
        val diff1 = MonkeyString("My name is johnny")
        val diff2 = MonkeyString("My name is johnny")

        assertThat(hello1.hashKey()).isEqualTo(hello2.hashKey())
        assertThat(diff1.hashKey()).isEqualTo(diff2.hashKey())
        assertThat(hello1.hashKey()).isNotEqualTo(diff1.hashKey())
    }

    @Test
    fun booleanHashKey() {
        val true1 = Boolean(true)
        val true2 = Boolean(true)
        val false1 = Boolean(false)
        val false2 = Boolean(false)

        assertThat(true1.hashKey()).isEqualTo(true2.hashKey())
        assertThat(false1.hashKey()).isEqualTo(false2.hashKey())
        assertThat(true1.hashKey()).isNotEqualTo(false1.hashKey())
    }

    @Test
    fun integerHashKey() {
        val one1 = Integer(1)
        val one2 = Integer(1)
        val two1 = Integer(2)
        val two2 = Integer(2)

        assertThat(one1.hashKey()).isEqualTo(one2.hashKey())
        assertThat(two1.hashKey()).isEqualTo(two2.hashKey())
        assertThat(one1.hashKey()).isNotEqualTo(two1.hashKey())
    }
}
