package dev.enro.ui

import dev.enro.stableClassName
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.OverlayTransitionsKey
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnroUiMetadataKeyNameTests {

    private val enroUiKeys: List<Pair<KClass<*>, String>> = listOf(
        NavigationDisplay.TransitionKey::class to NavigationDisplay.TransitionKey.name,
        NavigationDisplay.PopTransitionKey::class to NavigationDisplay.PopTransitionKey.name,
        NavigationDisplay.PredictivePopTransitionKey::class to NavigationDisplay.PredictivePopTransitionKey.name,
        DialogSceneStrategy.DialogPropertiesKey::class to DialogSceneStrategy.DialogPropertiesKey.name,
        OverlayTransitionsKey::class to OverlayTransitionsKey.name,
    )

    // stableClassName returns the qualified name where the platform provides one, and the
    // simple name on Kotlin/JS. Earlier versions stored stableClassName as the key name.
    @Test
    fun enroUiMetadataKeyNames_matchTheNamesUsedByEarlierVersions() {
        enroUiKeys.forEach { (keyClass, name) ->
            val stableName = stableClassName(keyClass)
            assertTrue(stableName == name || stableName == name.substringAfterLast('.'), "$stableName vs $name")
        }
    }

    @Test
    fun enroUiMetadataKeyNames_areUnique() {
        val names = enroUiKeys.map { it.second }
        assertEquals(names.size, names.toSet().size)
    }
}
