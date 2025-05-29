package dev.enro.tests.application

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import dev.enro.*
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val navigationContainer = NavigationContainer()
        navigationContainer.backstack += NavigationInstruction.Open(ListKey())
        destinations[ListKey::class] = listDestination
        destinations[DetailKey::class] = detailDestination

        setContent {
//            MaterialTheme {
//                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
//                    val container = rememberNavigationContainer(
//                        root = SelectDestination,
//                        emptyBehavior = EmptyBehavior.CloseParent
//                    )
//                    container.Render()
//                }
//            }
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                    NavigationDisplay(
                        container = navigationContainer,
                    )
                }
            }
        }
        applyInsetsForContentView()
    }
}

@Serializable
class ListKey : NavigationKey

val listDestination = navigationDestination<DetailKey> {
    val container = LocalNavigationContainer.current
    TitledColumn("List") {
        repeat(10) {
            Button(onClick = {
                container.backstack.add(NavigationInstruction.Open(DetailKey(it.toString())))
            }) {
                Text("Open Detail $it")
            }
        }
    }
}

class DetailKey(
    val id: String,
) : NavigationKey

val detailDestination = navigationDestination<DetailKey> {
    val context = LocalNavigationContext.current
    val key = context.destination.instruction.navigationKey as DetailKey
    TitledColumn("Details") {
        Text("id: ${key.id}")
        Button(onClick = {
            context.parentContainer.backstack.remove(context.destination.instruction)
        }) {
            Text("Close")
        }
    }
}