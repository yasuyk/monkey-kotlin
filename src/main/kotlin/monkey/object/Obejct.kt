package monkey.`object`

enum class ObjectType {
    INTEGER,
    BOOLEAN,
    NULL,
    RETURN_VALUE,
    ERROR,
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
