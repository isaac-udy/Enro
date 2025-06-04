package dev.enro.tests.application.window

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.requestClose
import dev.enro.navigationHandle
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn

@NavigationDestination(SimpleWindow::class)
class SimpleWindowActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val navigation = navigationHandle<NavigationKey>()
            TitledColumn(title = "Simple Window") {
                Button(onClick = { navigation.requestClose() }) {
                    Text(text = "Close Window")
                }
            }
        }
        applyInsetsForContentView()
    }
}