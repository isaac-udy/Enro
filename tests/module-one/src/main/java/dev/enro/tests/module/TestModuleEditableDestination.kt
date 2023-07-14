package dev.enro.tests.module

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

/**
 * This NavigationKey and associated NavigationDestination are used from inside of the
 * ProjectChangeTests of the :tests:application module. This file is modified/renamed/deleted/etc
 * as part of an incremental build which is run during the test to check that the KSP processor
 * correctly generates the associated binding files and compiles correctly.
 */
@Parcelize
internal class TestModuleEditableDestination : NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestModuleEditableDestination::class)
internal fun TestModuleEditableScreen() {
    Text("Test Screen")
}