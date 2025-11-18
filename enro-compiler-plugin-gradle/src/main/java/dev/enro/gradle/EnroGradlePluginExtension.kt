package dev.enro.gradle

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class EnroGradlePluginExtension @Inject constructor(objects: ObjectFactory) {
    /** Flag to enable/disable the plugin on this specific compilation. */
    public val enabled: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType).convention(true)
}