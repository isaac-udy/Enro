package dev.enro.path

import dev.enro.NavigationKey
import kotlin.reflect.KClass


public class NavigationPathBinding<T : NavigationKey> @PublishedApi internal constructor(
    internal val keyType: KClass<T>,
    internal val pattern: PathPattern,
    internal val deserialize: (PathData) -> T,
    internal val serialize: PathData.Builder.(T) -> Unit,
) {
    public constructor(
        keyType: KClass<T>,
        pattern: String,
        deserialize: PathData.() -> T,
        serialize: PathData.Builder.(T) -> Unit,
    ) : this(
        keyType = keyType,
        pattern = PathPattern.fromString(pattern),
        deserialize = deserialize,
        serialize = serialize
    )

    public fun matches(path: ParsedPath) : Boolean {
        return pattern.matches(path)
    }

    public fun matches(key: NavigationKey): Boolean {
        return keyType.isInstance(key)
    }

    public fun fromPath(path: ParsedPath): T {
        if (!matches(path)) {
            throw IllegalArgumentException("Path does not match the pattern")
        }
        val data = pattern.toPathData(path)
        return deserialize(data)
    }

    public fun toPath(key: T): String {
        val builder = PathData.Builder()
        builder.serialize(key)
        return pattern.toPath(builder.build())
    }

    public companion object {
        /**
         * Returns the binding in [bindings] that [matches][NavigationPathBinding.matches]
         * [path]. When more than one binding matches, the binding with the most literal path
         * segments is returned, and then the binding with the most required query parameters.
         *
         * Returns `null` when no binding matches. Throws when more than one matching binding
         * has the highest specificity.
         */
        public fun resolveForPath(
            bindings: List<NavigationPathBinding<*>>,
            path: ParsedPath,
        ): NavigationPathBinding<*>? {
            val matching = bindings.filter { it.matches(path) }
            if (matching.isEmpty()) return null
            if (matching.size == 1) return matching.single()

            val topScore = matching.maxOf { it.pattern.specificityScore }
            val mostSpecific = matching.filter { it.pattern.specificityScore == topScore }
            require(mostSpecific.size == 1) {
                "Multiple path bindings found for path: $path"
            }
            return mostSpecific.single()
        }
    }
}
