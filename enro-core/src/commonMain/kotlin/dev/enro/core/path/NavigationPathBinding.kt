package dev.enro.core.path

import dev.enro.core.NavigationKey
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
