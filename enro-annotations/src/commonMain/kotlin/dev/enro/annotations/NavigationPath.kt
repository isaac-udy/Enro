package dev.enro.annotations

import kotlin.reflect.KClass


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
public annotation class NavigationPath(
    val pattern: String,
) {
    /**
     * Binds a [NavigationKey][dev.enro.NavigationKey] to a path through an implementation
     * of `NavigationKey.PathBinding<T>`. Use FromBinding when the property mapping of
     * [NavigationPath] cannot express the path: for example, when a property has a default
     * value that is not in the URL.
     *
     * [binding] must be a class (typically a nested `object` on the key class) that
     * implements `dev.enro.NavigationKey.PathBinding<T>`, where `T` is the annotated key
     * type.
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS)
    public annotation class FromBinding(
        val binding: KClass<out Any>,
    )
}
