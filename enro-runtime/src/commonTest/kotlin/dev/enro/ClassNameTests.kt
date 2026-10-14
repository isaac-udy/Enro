package dev.enro

import dev.enro.platform.callSiteClassName
import dev.enro.result.flow.flowStepId
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

/** Kotlin/JS has no `KClass.qualifiedName`, so Enro names classes by their simple name there. */
internal expect val platformHasQualifiedClassNames: Boolean

class ClassNameTests {

    private fun expectedName(simpleName: String): String = when {
        platformHasQualifiedClassNames -> "dev.enro.ClassNameTests.$simpleName"
        else -> simpleName
    }

    @Test
    fun stableClassName_isTheQualifiedName() {
        assertEquals(expectedName("Named"), stableClassName(Named::class))
    }

    @Test
    fun callSiteClassName_isTheQualifiedNameForNamedClasses() {
        val expected = if (platformHasQualifiedClassNames) expectedName("Named") else null
        assertEquals(expected, callSiteClassName(Named::class))
    }

    @Test
    fun metadataKeyNames_areTheQualifiedName() {
        assertEquals(expectedName("KeyMetadata"), KeyMetadata.name)
        assertEquals(expectedName("DestinationMetadata"), DestinationMetadata.name)
        assertEquals(expectedName("SceneMetadata"), SceneMetadata.name)
    }

    @Test
    fun metadataKeyNames_useTheExplicitNameWhenGiven() {
        assertEquals("explicit.key", ExplicitKeyMetadata.name)
        assertEquals("explicit.transient", ExplicitTransientMetadata.name)
        assertEquals("explicit.destination", ExplicitDestinationMetadata.name)
        assertEquals("explicit.scene", ExplicitSceneMetadata.name)
    }

    @Test
    fun flowStepId_isStablePerCallSite() {
        fun callSite() = flowStepId<StepKey>()

        assertEquals(callSite(), callSite())
    }

    class Named

    @Serializable
    class StepKey : NavigationKey

    object KeyMetadata : NavigationKey.MetadataKey<Int>(0)

    private object DestinationMetadata : NavigationDestination.MetadataKey<Int>(0)

    private object SceneMetadata : NavigationScene.MetadataKey<Int>(0)

    object ExplicitKeyMetadata : NavigationKey.MetadataKey<Int>(0, name = "explicit.key")

    object ExplicitTransientMetadata : NavigationKey.TransientMetadataKey<Int>(0, name = "explicit.transient")

    private object ExplicitDestinationMetadata : NavigationDestination.MetadataKey<Int>(0, name = "explicit.destination")

    private object ExplicitSceneMetadata : NavigationScene.MetadataKey<Int>(0, name = "explicit.scene")
}
