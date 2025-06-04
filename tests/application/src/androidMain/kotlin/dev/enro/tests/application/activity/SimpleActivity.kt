package dev.enro.tests.application.activity

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.requestClose
import dev.enro.navigationHandle
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object SimpleActivity : Parcelable, NavigationKey.SupportsPresent


@NavigationDestination(SimpleActivity::class)
class SimpleActivityImpl : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val navigation = navigationHandle<NavigationKey>()
            TitledColumn(title = "Simple Activity") {
                Button(onClick = { navigation.requestClose() }) {
                    Text(text = "Close Activity")
                }
            }
        }
        applyInsetsForContentView()
    }
}