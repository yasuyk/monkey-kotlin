package monkey.`object`

enum class ObjectType(@Suppress("UNUSED_PARAMETER") type: String) {
    INTEGER("INTEGER"),
    BOOLEAN("BOOLEAN"),
    NULL("NULL"),
}


interface Object {
    fun type(): ObjectType
    fun inspect(): String
}

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
