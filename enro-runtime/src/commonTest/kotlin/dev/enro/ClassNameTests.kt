package dev.enro

import dev.enro.platform.callSiteClassName
import dev.enro.result.flow.flowStepId
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassNameTests {

    @Test
    fun stableClassName_isTheQualifiedName() {
        assertEquals("dev.enro.ClassNameTests.Named", stableClassName(Named::class))
    }

    @Test
    fun callSiteClassName_isTheQualifiedNameForNamedClasses() {
        assertEquals("dev.enro.ClassNameTests.Named", callSiteClassName(Named::class))
    }

    @Test
    fun metadataKeyNames_areTheQualifiedName() {
        assertEquals("dev.enro.ClassNameTests.KeyMetadata", KeyMetadata.name)
        assertEquals("dev.enro.ClassNameTests.DestinationMetadata", DestinationMetadata.name)
        assertEquals("dev.enro.ClassNameTests.SceneMetadata", SceneMetadata.name)
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
}
