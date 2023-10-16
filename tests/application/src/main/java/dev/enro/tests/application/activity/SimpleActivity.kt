package dev.enro.tests.application.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.requestClose
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object SimpleActivity : NavigationKey.SupportsPresent

@NavigationDestination(SimpleActivity::class)
class SimpleActivityImpl : ComponentActivity() {

    private val navigation by navigationHandle<NavigationKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TitledColumn(title = "Simple Activity") {
                Button(onClick = { navigation.requestClose() }) {
                    Text(text = "Close Activity")
                }
            }
        }
    }
}