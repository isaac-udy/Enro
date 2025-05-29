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

package androidx.navigation3.runtime

import androidx.compose.runtime.Composable

/**
 * Class that wraps a [NavEntry] within another [NavEntry].
 *
 * This provides a nesting mechanism for [NavEntry]s that allows properly nested content.
 *
 * @param navEntry the [NavEntry] to wrap
 */
public open class NavEntryWrapper<T : Any>(public val navEntry: NavEntry<T>) :
    NavEntry<T>(navEntry.key, navEntry.metadata, navEntry.content) {
    override val key: T
        get() = navEntry.key

    override val metadata: Map<String, Any>
        get() = navEntry.metadata

    override val content: @Composable (T) -> Unit
        get() = navEntry.content
}
