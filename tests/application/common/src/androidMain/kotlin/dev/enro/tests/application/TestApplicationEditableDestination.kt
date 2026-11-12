package dev.enro.tests.application

import android.os.Parcelable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize


/**
 * ProjectChangeTests edits, renames and deletes this file, then runs an incremental
 * build to check that the KSP processor regenerates the bindings and the project compiles.
 */
@Parcelize
internal class TestApplicationEditableDestination : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestApplicationEditableDestination::class)
internal fun TestApplicationEditableScreen() {
    Text("Test Screen")
}