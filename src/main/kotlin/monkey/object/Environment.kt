package monkey.`object`

class Environment(
    private val store: MutableMap<String, Object?> = mutableMapOf(),
    private val outer: Environment? = null
) {
    operator fun get(key: String): Object? {
        return store[key] ?: outer?.get(key)
    }

    operator fun set(key: String, value: Object?) {
        store[key] = value
    }

    companion object {
        fun newEnclosedEnvironment(outer: Environment) = Environment(outer = outer)
    }
}
