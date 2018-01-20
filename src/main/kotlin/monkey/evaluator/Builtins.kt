package monkey.evaluator


import monkey.`object`.Builtin
import monkey.`object`.Error
import monkey.`object`.Integer
import monkey.`object`.MonkeyArray
import monkey.`object`.MonkeyString
import monkey.`object`.ObjectType

val builtins = mapOf(
    "len" to Builtin({ args ->
        if (args.size != 1) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=1")
        }
        val arg = args[0]
        when (arg) {
            is MonkeyArray -> Integer(arg.elements.size.toLong())
            is MonkeyString -> Integer(arg.value.length.toLong())
            else -> Error("argument to `len` not supported, got ${args[0]?.type()}")
        }
    }),
    "first" to Builtin({ args ->
        if (args.size != 1) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=1")
        }
        if (args[0]?.type() != ObjectType.ARRAY) {
            return@Builtin Error("argument to `first` must be ARRAY, got ${args[0]?.type()}")
        }

        val array = args[0] as MonkeyArray
        if (array.elements.isEmpty()) {
            return@Builtin NULL
        }
        array.elements.first() ?: NULL
    }),
    "last" to Builtin({ args ->
        if (args.size != 1) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=1")
        }
        if (args[0]?.type() != ObjectType.ARRAY) {
            return@Builtin Error("argument to `last` must be ARRAY, got ${args[0]?.type()}")
        }

        val array = args[0] as MonkeyArray
        if (array.elements.isEmpty()) {
            return@Builtin NULL
        }
        array.elements.last() ?: NULL
    }),
    "rest" to Builtin({ args ->
        if (args.size != 1) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=1")
        }
        if (args[0]?.type() != ObjectType.ARRAY) {
            return@Builtin Error("argument to `rest` must be ARRAY, got ${args[0]?.type()}")
        }

        val array = args[0] as MonkeyArray
        if (array.elements.isEmpty()) {
            return@Builtin NULL
        }

        MonkeyArray(array.elements.subList(1, array.elements.size))
    }),
    "push" to Builtin({ args ->
        if (args.size != 2) {
            return@Builtin Error("wrong number of arguments. got=${args.size}, want=2")
        }
        if (args[0]?.type() != ObjectType.ARRAY) {
            return@Builtin Error("argument to `push` must be ARRAY, got ${args[0]?.type()}")
        }

        val array = args[0] as MonkeyArray

        val new = array.elements.toMutableList()
        new.add(args[1])
        MonkeyArray(new)
    }),
    "puts" to Builtin({ args ->
        for (arg in args) {
            println(arg?.inspect())
        }
        NULL
    })
)
