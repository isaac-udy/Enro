package dev.enro.tests.application.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.close
import dev.enro.complete
import dev.enro.core.NavigationKey
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.putNavigationKeyInstance
import kotlinx.parcelize.Parcelize

@Parcelize
object SimpleActivity : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(SimpleActivity::class)
class SimpleActivityImpl : ComponentActivity() {
    private val navigation by navigationHandle<SimpleActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TitledColumn(title = "Simple Activity") {
                Button(onClick = {
                    navigation.open(SimpleActivity)
                }) {
                    Text(text = "Open through NavigationHandle")
                }
                Button(onClick = {
                    startActivity(
                        Intent(this@SimpleActivityImpl, SimpleActivityImpl::class.java)
                            .putNavigationKeyInstance(SimpleActivity.asInstance())
                    )
                }) {
                    Text(text = "Open through Intent")
                }
                Button(onClick = { navigation.close() }) {
                    Text(text = "Close")
                }
                Button(onClick = { navigation.complete() }) {
                    Text(text = "Complete")
                }
                Button(onClick = { finish() }) {
                    Text(text = "Finish")
                }
            }
        }
        applyInsetsForContentView()
    }
}