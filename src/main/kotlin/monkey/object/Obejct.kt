package monkey.`object`

import monkey.ast.BlockStatement
import monkey.ast.Identifier

enum class ObjectType {
    INTEGER,
    BOOLEAN,
    NULL,
    RETURN_VALUE,
    ERROR,
    FUNCTION,
    STRING,
    BUILTIN,
    ARRAY,
}


interface Object {
    fun type(): ObjectType
    fun inspect(): String
}

fun Object?.isError() = this?.type() == ObjectType.ERROR

class Integer(val value: Long) : Object {
    override fun type() = ObjectType.INTEGER
    override fun inspect() = value.toString()
}

class Boolean(val value: kotlin.Boolean) : Object {
    override fun type() = ObjectType.BOOLEAN
    override fun inspect() = value.toString()
}

class Null : Object {
    override fun type() = ObjectType.NULL
    override fun inspect() = "null"
}

class ReturnValue(val value: Object?) : Object {
    override fun type() = ObjectType.RETURN_VALUE
    override fun inspect() = value?.inspect() ?: ""
}

class Error(val message: String) : Object {
    override fun type() = ObjectType.ERROR
    override fun inspect() = "ERROR: $message"
}

class Function(
    val parameters: List<Identifier>,
    val body: BlockStatement,
    val env: Environment
) : Object {
    override fun type() = ObjectType.FUNCTION
    override fun inspect(): String {
        return StringBuffer().apply {
            append("fn")
            append("(")
            append(parameters.joinToString { p -> p.string() })
            append(") {\n")
            append(body.string())
            append("\n}")
        }.toString()
    }
}

class MonkeyString(val value: String) : Object {
    override fun type() = ObjectType.STRING
    override fun inspect() = value
}

typealias BuiltinFunction = (args: List<Object?>) -> Object

class Builtin(val fn: BuiltinFunction) : Object {
    override fun type() = ObjectType.BUILTIN
    override fun inspect() = "builtin function"
}

class MonkeyArray(val elements: List<Object?>) : Object {
    override fun type() = ObjectType.ARRAY
    override fun inspect(): String {
        return StringBuffer().apply {
            append("[")
            append(elements.joinToString { e -> e?.inspect() ?: "" })
            append("]")
        }.toString()
    }
}
