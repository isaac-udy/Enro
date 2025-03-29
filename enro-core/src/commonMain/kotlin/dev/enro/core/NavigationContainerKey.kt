package dev.enro.core

import dev.enro.core.internal.EnroSerializable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public sealed class NavigationContainerKey {
    public abstract val name: String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false

        other as NavigationContainerKey

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "NavigationContainerKey($name)"
    }

    public class Dynamic private constructor(
        override val name: String
    ) : NavigationContainerKey(), EnroSerializable {
        public constructor() : this("DynamicContainerKey(${Uuid.random()})")
    }

    public class FromName(
        override val name: String
    ) : NavigationContainerKey()

    public class FromId private constructor(
        public val id: Int,
        override val name: String
    ) : NavigationContainerKey(), EnroSerializable {
        public constructor(id: Int) : this(
            id = id,
            name = "FromId($id)"
        )
    }
}