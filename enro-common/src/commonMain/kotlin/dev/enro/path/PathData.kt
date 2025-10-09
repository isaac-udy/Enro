package dev.enro.path

public class PathData(
    internal val data: MutableMap<String, String> = mutableMapOf(),
) {
    public fun optional(key: String): String? {
        return data[key]
    }

    public fun require(key: String): String {
        return requireNotNull(data[key])
    }

    public class Builder internal constructor() {
        private val data = mutableMapOf<String, String>()

        public fun set(key: String, value: String) {
            data[key] = value
        }

        internal fun build(): PathData {
            return PathData(data)
        }
    }
}