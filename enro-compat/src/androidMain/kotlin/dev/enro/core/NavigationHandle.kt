package dev.enro.core

import androidx.fragment.app.Fragment
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.closeWithoutCallback
import dev.enro.complete
import dev.enro.open
import dev.enro.withMetadata
import kotlin.properties.ReadOnlyProperty
import dev.enro.navigationHandle as fragmentNavigationHandle

public typealias NavigationHandle = dev.enro.NavigationHandle<out NavigationKey>
public typealias TypedNavigationHandle<T> =  dev.enro.NavigationHandle<T>

public val NavigationHandle<*>.instruction: AnyOpenInstruction
    get() = this.instance

public fun dev.enro.NavigationHandle<*>.present(key: dev.enro.core.NavigationKey.SupportsPresent) {
    open(
        key.withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Present,
        )
    )
}

public fun dev.enro.NavigationHandle<*>.push(key: dev.enro.core.NavigationKey.SupportsPush) {
    open(
        key.withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Push,
        )
    )
}

public fun dev.enro.NavigationHandle<*>.close() {
    closeWithoutCallback()
}

public fun dev.enro.NavigationHandle<*>.requestClose() {
    close()
}

public fun <R: Any> dev.enro.NavigationHandle<out NavigationKey.WithResult<R>>.closeWithResult(result: R) {
    complete(result)
}


public fun <T : NavigationKey> Fragment.navigationHandle() : ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return fragmentNavigationHandle()
}

