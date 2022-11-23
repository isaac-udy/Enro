package dev.enro.core

import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize
import java.util.*

public sealed class NavigationContainerKey : Parcelable  {
    public abstract val name: String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationContainerKey

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    @Parcelize
    public class Dynamic private constructor(
        override val name: String
    ) : NavigationContainerKey() {
        public constructor() : this("DynamicContainerKey(${UUID.randomUUID()})")
    }

    @Parcelize
    public class FromName(
        override val name: String
    ) : NavigationContainerKey()

    @Parcelize
    public class FromId private constructor(
        @IdRes public val id: Int,
        override val name: String
    ) : NavigationContainerKey() {
        public constructor(@IdRes id: Int) : this(
            id = id,
            name = "FromId($id)"
        )
    }
}

