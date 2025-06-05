package dev.enro.tests.application

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import dev.enro.asInstance
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
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