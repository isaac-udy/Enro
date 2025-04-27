package dev.enro.core

import kotlin.reflect.KClass

public abstract class NavigationBinding<KeyType : NavigationKey, ContextType : Any> {
    public abstract val keyType: KClass<KeyType>
    public abstract val keySerializer: NavigationKeySerializer<KeyType>
    public abstract val destinationType: KClass<ContextType>
    public abstract val baseType: KClass<in ContextType>

    internal var isPlatformOverride: Boolean = false
}


// TODO these should be moved to a different file, and need javadocs explaining how they work
internal const val USE_ORIGINAL_NAVIGATION_BINDING = "dev.enro.core.USE_ORIGINAL_NAVIGATION_BINDING"

public fun <T: NavigationKey> T.useOriginalBinding(): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = this,
        extras = NavigationInstructionExtras().useOriginalBinding()
    )
}

public fun <T: NavigationKey> NavigationKey.WithExtras<T>.useOriginalBinding(): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = navigationKey,
        extras = extras.useOriginalBinding()
    )
}

public fun NavigationInstructionExtras.useOriginalBinding(): NavigationInstructionExtras {
    return NavigationInstructionExtras().apply {
        putAll(this@useOriginalBinding)
        put(USE_ORIGINAL_NAVIGATION_BINDING, true)
    }
}

public fun NavigationInstructionExtras.shouldUseOriginalBinding(): Boolean {
    return get<Boolean>(USE_ORIGINAL_NAVIGATION_BINDING) == true
}
