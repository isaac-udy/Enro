@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.leafContext
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.destination.ios.hosts.HostUIViewControllerInCompose
import dev.enro.tests.application.ios.NativeSwiftUIView
import kotlinx.serialization.Serializable

@Serializable
object MainView : NavigationKey.SupportsPresent

@NavigationDestination(MainView::class)
@Composable
fun MainViewController() {
    val navigation = navigationHandle()
    val container = rememberNavigationContainer(
        root = SelectDestination,
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    Column(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        IconButton(
            onClick = {
                container.context.leafContext().navigationHandle.requestClose()
            }
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
        }
        Button(
            onClick = {
                navigation.push(
                    HostUIViewControllerInCompose(
                        NativeSwiftUIView.asPush()
                    )
                )
            }
        ) {
            Text("Show Nested")
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            container.Render()
        }

    }
}

@Composable
fun ExampleTest() {
    val thing = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .fillMaxSize(),
    ) {
        val controller = LocalUIViewController.current
        Text(
            text = "Controller: $controller, ${controller.parentViewController()}",
        )
        Button(
            onClick = {
                thing.value = !thing.value
            }
        ) {
            Text("Show Nested")
        }
        if (thing.value) {
            UIKitViewController(
                modifier = Modifier.fillMaxWidth().fillMaxSize(),
                factory = { ComposeUIViewController { ExampleTest() } }
            )
        }
    }
}


/*
Ideas for Enro in iOS:
Create an "EnroViewController" class, which should be used as the root UIViewController for a particular
Enro heirarchy. Within this UIViewController, we should be able to host both SwiftUI and UIView content,
using the Composable UIKitViewController function.

The idea here would be that an EnroViewController would take a NavigationInstruction as a
constructor parameter and would render only the Composable associated with that NavigationInstruction,
so this would be the root of the Enro hierarchy.

May need to consider how this works for multiple EnroViewControllers in the same heirarchy,
or how this would work for the WindowManager.

It appears that LocalUIViewController.current will correctly have the parent composable
hosting viewcontroller as the parent, which would allow us to go up and down the heirarchy from the
viewcontroller reference to find the nearest navigation handle etc etc

 */
