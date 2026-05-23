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

    /**
     * How specific this binding's pattern is. When multiple bindings [matches] the
     * same path, [getPathBinding][dev.enro.controller.repository.PathRepository.getPathBinding]
     * picks the one with the highest specificity. Higher = more specific.
     */
    public val specificity: Int get() = pattern.specificityScore

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

    public companion object
}
