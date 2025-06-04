package dev.enro.core

import android.os.Parcelable
import dev.enro.asInstance
import dev.enro.withMetadata

public interface NavigationKey : dev.enro.NavigationKey, Parcelable {
    public interface WithResult<T: Any> : NavigationKey, dev.enro.NavigationKey.WithResult<T>

    public interface SupportsPush : NavigationKey {
        public interface WithResult<T: Any> : SupportsPush, NavigationKey.WithResult<T>
    }

    public interface SupportsPresent : NavigationKey {
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