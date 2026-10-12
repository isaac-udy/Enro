package dev.enro

import dev.enro.controller.interceptors.PreviouslyActiveContainerInterceptor
import dev.enro.result.NavigationResultChannel
import dev.enro.result.flow.FlowResultManager
import dev.enro.result.flow.FlowStep
import dev.enro.result.flow.NavigationFlow
import dev.enro.result.flow.NavigationFlowReference
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class EnroMetadataKeyNameTests {

    private val enroKeys: List<Pair<KClass<*>, String>> = listOf(
        NavigationHandleConfiguration.OnCloseCallbacks::class to NavigationHandleConfiguration.OnCloseCallbacks.name,
        NavigationBinding.Companion.UseOriginalBindingKey::class to NavigationBinding.Companion.UseOriginalBindingKey.name,
        NavigationResultChannel.ResultIdKey::class to NavigationResultChannel.ResultIdKey.name,
        NavigationFlow.Companion.ResultFlowKey::class to NavigationFlow.Companion.ResultFlowKey.name,
        NavigationFlow.Companion.ResultFlowIdKey::class to NavigationFlow.Companion.ResultFlowIdKey.name,
        FlowStep.Id.MetadataKey::class to FlowStep.Id.MetadataKey.name,
        FlowResultManager.Companion.FlowResultManagerKey::class to FlowResultManager.Companion.FlowResultManagerKey.name,
        NavigationFlowReference.MetadataKey::class to NavigationFlowReference.MetadataKey.name,
        PreviouslyActiveContainerInterceptor.PreviouslyActiveContainer::class to PreviouslyActiveContainerInterceptor.PreviouslyActiveContainer.name,
        DirectOverlaySceneStrategy.IsDirectOverlayKey::class to DirectOverlaySceneStrategy.IsDirectOverlayKey.name,
        DirectOverlaySceneStrategy.OverlayFadeKey::class to DirectOverlaySceneStrategy.OverlayFadeKey.name,
    )

    @Test
    fun enroMetadataKeyNames_matchTheNamesPersistedByEarlierVersions() {
        enroKeys.forEach { (keyClass, name) ->
            if (platformHasQualifiedClassNames) {
                assertEquals(stableClassName(keyClass), name)
            } else {
                assertEquals(stableClassName(keyClass), name.substringAfterLast('.'))
            }
        }
    }

    @Test
    fun enroMetadataKeyNames_areUnique() {
        val names = enroKeys.map { it.second }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun anApplicationKeyNamedMetadataKey_doesNotClashWithEnroKeys() {
        assertEquals("dev.enro.result.flow.FlowStep.Id.MetadataKey", FlowStep.Id.MetadataKey.name)
        assertEquals(
            if (platformHasQualifiedClassNames) "dev.enro.EnroMetadataKeyNameTests.MetadataKey" else "MetadataKey",
            MetadataKey.name,
        )
    }

    object MetadataKey : NavigationKey.MetadataKey<Int>(0)
}
