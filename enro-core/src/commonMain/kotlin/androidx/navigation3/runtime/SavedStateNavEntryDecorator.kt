/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.navigation3.runtime

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.*
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner

/**
 * Returns a [SavedStateNavEntryDecorator] that is remembered across recompositions.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that scopes the returned NavEntryDecorator
 */
@Composable
public fun rememberSavedStateNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder()
): NavEntryDecorator<Any> = remember { SavedStateNavEntryDecorator(saveableStateHolder) }

/**
 * Wraps the content of a [NavEntry] with a [SaveableStateHolder.SaveableStateProvider] to ensure
 * that calls to [rememberSaveable] within the content work properly and that state can be saved.
 * Also provides the content of a [NavEntry] with a [SavedStateRegistryOwner] which can be accessed
 * in the content with [LocalSavedStateRegistryOwner].
 *
 * This [NavEntryDecorator] is the only one that is **required** as saving state is considered a
 * non-optional feature.
 */
public fun SavedStateNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder
): NavEntryDecorator<Any> {
    val registryMap = mutableMapOf<String, EntrySavedStateRegistry>()

    val onPop: (Any) -> Unit = { key ->
        val id = getIdForKey(key)
        if (registryMap.contains(id)) {
            // saveableStateHolder onPop
            saveableStateHolder.removeState(id)

            // saved state onPop
            val savedState = savedState()
            val childRegistry = registryMap.getValue(id)
            childRegistry.savedStateRegistryController.performSave(savedState)
            childRegistry.savedState = savedState
            childRegistry.lifecycle.currentState = Lifecycle.State.DESTROYED
        }
    }

    return navEntryDecorator(onPop = onPop) { entry ->
        val key = entry.key
        val id = getIdForKey(key)

        val childRegistry by
            rememberSaveable(
                key,
                stateSaver =
                    Saver(
                        save = { it.savedState },
                        restore = { EntrySavedStateRegistry().apply { savedState = it } },
                    ),
            ) {
                mutableStateOf(EntrySavedStateRegistry())
            }
        registryMap.put(id, childRegistry)

        saveableStateHolder.SaveableStateProvider(id) {
            CompositionLocalProvider(EnroLocalSavedStateRegistryOwner provides childRegistry) {
                entry.content(key)
            }
        }
        childRegistry.lifecycle.currentState = Lifecycle.State.RESUMED
    }
}

private fun getIdForKey(key: Any): String = "${key::class.qualifiedName}:$key"

internal class EntrySavedStateRegistry : SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    var savedState: SavedState? = null

    init {
        savedStateRegistryController.performRestore(savedState)
    }
}
