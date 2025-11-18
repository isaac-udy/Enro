package dev.enro.tests.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationModule
import kotlin.reflect.KClass

@Composable
@NavigationDestination(ModuleOneDestination::class)
fun ModuleOneScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Module One",
            style = MaterialTheme.typography.subtitle1,
        )
    }
}

object _GeneratedBinding_TestModuleEditableDestination
fun NavigationModule.bind(
    reference: _GeneratedBinding_TestModuleEditableDestination
) {

}

object _GeneratedBinding_ModuleOneDestination
fun NavigationModule.bind(
    reference: _GeneratedBinding_ModuleOneDestination
) {

}

object _GeneratedBinding_NavigationKey
inline fun <reified T : NavigationKey> NavigationModule.bind(
    reference: _GeneratedBinding_NavigationKey
) {

}