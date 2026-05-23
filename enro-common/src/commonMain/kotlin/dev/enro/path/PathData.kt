package dev.enro.path

public class PathData internal constructor(
    internal val data: Map<String, String>,
) {
    public fun optional(key: String): String? {
        return data[key]
    }

    public fun require(key: String): String {
        return requireNotNull(data[key]) {
            "No value found for path parameter '$key'"
        }
    }

    public class Builder internal constructor() {
        private val data = mutableMapOf<String, String>()

        public fun set(key: String, value: String) {
            data[key] = value
        }

        internal fun build(): PathData {
            return PathData(data.toMap())
        }
    }
}
