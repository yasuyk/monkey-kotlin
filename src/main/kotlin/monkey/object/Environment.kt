package monkey.`object`

class Environment(private val store: MutableMap<String, Object?> = mutableMapOf()) {
    operator fun get(key: String): Object? = store[key]
    operator fun set(key: String, value: Object?) {
        store[key] = value
    }
}
