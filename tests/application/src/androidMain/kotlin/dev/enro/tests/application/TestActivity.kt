package dev.enro.tests.application

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import dev.enro.asInstance
import dev.enro.context.getDebugString
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalNavigationContext.current
            LaunchedEffect(context) {
                var string = ""
                while(isActive) {
                    delay(1000)
                    val newString = context.getDebugString()
                    if (newString != string) {
                        string = newString
                        Log.e("Enro", string)
                    }
                }
            }
            val container = rememberNavigationContainer(
                backstack = listOf(SelectDestination().asInstance()),
            )
            MaterialTheme {
                NavigationDisplay(container)
            }
        }
        applyInsetsForContentView()
    }
}