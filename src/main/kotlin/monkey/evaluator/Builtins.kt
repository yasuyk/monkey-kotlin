package monkey.evaluator


import monkey.`object`.Builtin
import monkey.`object`.Error
import monkey.`object`.Integer
import monkey.`object`.MonkeyString

val builtins = mapOf(
    "len" to Builtin({ args ->
        if (args.size != 1) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=1")
        }
        val arg = args[0] as? MonkeyString
        if (arg != null) {
            return@Builtin Integer(arg.value.length.toLong())
        }
        Error("argument to `len` not supported, got ${args[0]?.type()}")
    })
)
