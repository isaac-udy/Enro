/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.navigation3.ui

import androidx.compose.runtime.*
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.navEntryDecorator

/** Returns a [SceneSetupNavEntryDecorator] that is remembered across recompositions. */
@Composable
public fun rememberSceneSetupNavEntryDecorator(): NavEntryDecorator<Any> = remember {
    SceneSetupNavEntryDecorator()
}

/**
 * A [NavEntryDecorator] that wraps each entry in a [movableContentOf] to allow nav displays to
 * arbitrarily place entries in different places in the composable call hierarchy and ensures that
 * the same entry content is not composed multiple times in different places of the hierarchy.
 *
 * This should likely be the first [NavEntryDecorator] to ensure that other [NavEntryDecorator]
 * calls that are stateful are moved properly inside the [movableContentOf].
 */
public fun SceneSetupNavEntryDecorator(): NavEntryDecorator<Any> {
    val movableContentContentHolderMap: MutableMap<Any, MutableState<@Composable () -> Unit>> =
        mutableMapOf()
    val movableContentHolderMap: MutableMap<Any, @Composable () -> Unit> = mutableMapOf()
    return navEntryDecorator { entry ->
        val key = entry.key
        movableContentContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    mutableStateOf(
                        @Composable {
                            error(
                                "Should not be called, this should always be updated in" +
                                    "DecorateEntry with the real content"
                            )
                        }
                    )
                }
            }
        }
        movableContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    movableContentOf {
                        // In case the key is removed from the backstack while this is still
                        // being rendered, we remember the MutableState directly to allow
                        // rendering it while we are animating out.
                        remember { movableContentContentHolderMap.getValue(key) }.value()
                    }
                }
            }
        }

        if (LocalEntriesToRenderInCurrentScene.current.contains(entry.key)) {
            key(key) {
                // In case the key is removed from the backstack while this is still
                // being rendered, we remember the MutableState directly to allow
                // updating it while we are animating out.
                val movableContentContentHolder = remember {
                    movableContentContentHolderMap.getValue(key)
                }
                // Update the state holder with the actual entry content
                movableContentContentHolder.value = { entry.content(key) }
                // In case the key is removed from the backstack while this is still
                // being rendered, we remember the movableContent directly to allow
                // rendering it while we are animating out.
                val movableContentHolder = remember { movableContentHolderMap.getValue(key) }
                // Finally, render the entry content via the movableContentOf
                movableContentHolder()
            }
        }
    }
}

/**
 * The entry keys to render in the current [Scene], in the sense of the target of the animation for
 * an [AnimatedContent] that is transitioning between different scenes.
 */
public val LocalEntriesToRenderInCurrentScene: ProvidableCompositionLocal<Set<Any>> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalEntriesToRenderInCurrentScene. You should only " +
                "access LocalEntriesToRenderInCurrentScene inside a NavEntry passed " +
                "to NavDisplay."
        )
    }
