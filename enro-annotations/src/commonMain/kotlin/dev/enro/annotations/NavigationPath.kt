package dev.enro.annotations

import kotlin.reflect.KClass


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
@ExperimentalEnroApi
public annotation class NavigationPath(
    val pattern: String,
) {
    /**
     * Declares that a [NavigationKey][dev.enro.NavigationKey] is bound to a path via a
     * user-implemented `NavigationKey.PathBinding<T>`. Use this for cases that don't
     * fit the simple property-based mapping provided by the parent [NavigationPath]
     * annotation — for example, when some properties get default values that aren't
     * present in the URL, or when the deserialize/serialize logic needs to be hand-written.
     *
     * The referenced [binding] must be a class (typically a nested `object` on the key
     * class) that implements `dev.enro.NavigationKey.PathBinding<T>` where `T` is the
     * key type itself.
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS)
    @ExperimentalEnroApi
    public annotation class FromBinding(
        val binding: KClass<out Any>,
    )
}
