package dev.enro.annotations

import kotlin.reflect.KClass

@Retention(value = AnnotationRetention.BINARY)
@Target(allowedTargets = [AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY])
public actual annotation class NavigationDestination actual constructor(actual val key: KClass<out Any>) {

    @Retention(value = AnnotationRetention.BINARY)
    @Target(allowedTargets = [AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY])
    public annotation class PlatformOverride(val key: KClass<out Any>)
}