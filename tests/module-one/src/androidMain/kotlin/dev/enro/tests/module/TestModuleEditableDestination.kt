package dev.enro.tests.module

import android.os.Parcelable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

/**
 * ProjectChangeTests (in :tests:application:common) edits, renames and deletes this file,
 * then runs an incremental build to check that the KSP processor regenerates the bindings
 * and the project compiles.
 */
@Parcelize
internal class TestModuleEditableDestination : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestModuleEditableDestination::class)
internal fun TestModuleEditableScreen() {
    Text("Test Screen")
}