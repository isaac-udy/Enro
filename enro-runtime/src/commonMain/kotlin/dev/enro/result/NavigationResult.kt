package dev.enro.result

import dev.enro.NavigationKey

public sealed class NavigationResult<K : NavigationKey> {
    internal abstract val instance: NavigationKey.Instance<NavigationKey>

    public class Closed(
        override val instance: NavigationKey.Instance<NavigationKey>
    ) : NavigationResult<NavigationKey>()

    public class Delegated(
        override val instance: NavigationKey.Instance<NavigationKey>
    ) : NavigationResult<NavigationKey>()

    public class Completed<K : NavigationKey>(
        @PublishedApi
        override val instance: NavigationKey.Instance<K>,

        @PublishedApi
        internal val data: Any?
    ) : NavigationResult<K>() {
        public companion object {
            public val <R : Any> Completed<out NavigationKey.WithResult<R>>.result: R get() {
                require(data != null) {
                    "Incorrect type, but got null"
                }
                @Suppress("UNCHECKED_CAST")
                return data as R
            }
        }
    }
}
