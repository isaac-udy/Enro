package dev.enro.tests.application

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                val container = rememberNavigationContainer(
                    root = SelectDestination,
                    emptyBehavior = EmptyBehavior.CloseParent
                )
                container.Render()
            }
        }
    }
}