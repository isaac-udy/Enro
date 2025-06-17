package dev.enro.core

import android.os.Parcelable
import dev.enro.asInstance
import dev.enro.withMetadata

@Suppress("DEPRECATION")
@Deprecated("Use dev.enro.NavigationKey")
public interface NavigationKey : dev.enro.NavigationKey, Parcelable {
    @Deprecated("Use dev.enro.NavigationKey.WithResult")
    public interface WithResult<T: Any> : NavigationKey, dev.enro.NavigationKey.WithResult<T>

    @Deprecated("Use dev.enro.NavigationKey")
    public interface SupportsPush : NavigationKey {
        @Deprecated("Use dev.enro.NavigationKey.WithResult")
        public interface WithResult<T: Any> : SupportsPush, NavigationKey.WithResult<T>
    }

    @Deprecated("Use dev.enro.NavigationKey")
    public interface SupportsPresent : NavigationKey {
        @Deprecated("Use dev.enro.NavigationKey.WithResult")
        public interface WithResult<T: Any> : SupportsPresent, NavigationKey.WithResult<T>
    }
}

public fun <T : NavigationKey.SupportsPush> T.asPush(): dev.enro.NavigationKey.Instance<T> {
    return withMetadata(
        NavigationDirection.MetadataKey,
        NavigationDirection.Push,
    ).asInstance()
}

public fun  <T : NavigationKey.SupportsPresent> T.asPresent(): dev.enro.NavigationKey.Instance<T> {
    return withMetadata(
        NavigationDirection.MetadataKey,
        NavigationDirection.Present,
    ).asInstance()
}

public fun <T: NavigationKey, M> T.withExtra(
    key: dev.enro.NavigationKey.MetadataKey<M>,
    value: M,
): dev.enro.NavigationKey.WithMetadata<T> {
    return withMetadata(key, value)
}

public fun <T: NavigationKey, M> dev.enro.NavigationKey.WithMetadata<T>.withExtra(
    key: dev.enro.NavigationKey.MetadataKey<M>,
    value: M,
): dev.enro.NavigationKey.WithMetadata<T> {
    return withMetadata(key, value)
}