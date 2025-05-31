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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import dev.enro.*
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        destinations[ListKey::class] = listDestination
        destinations[DetailKey::class] = detailDestination
        destinations[ResultKey::class] = resultDestination

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
            val container = rememberNavigationContainer(
                backstack = listOf(ListKey().asInstance()),
            )
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                    NavigationDisplay(
                        container = container,
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
    val navigation = navigationHandle<DetailKey>()
    val result = rememberSaveable { mutableStateOf("") }
    val stringResultChannel = registerForNavigationResult<String>(
        onClosed = {
            result.value = "Closed string"
        }
    ) {
        result.value = it
    }
    val resultChannel = registerForNavigationResult(
        onClosed = {
            result.value = "Closed reg"
        }
    ) {
        result.value = "Completed reg"
    }

    TitledColumn("List") {
        Text("Result: ${result.value}")
        Button(onClick = {
            stringResultChannel.open(ResultKey())
        }) {
            Text("Get Result")
        }
        repeat(10) {
            Button(onClick = {
                resultChannel.open(DetailKey(it.toString()))
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
    val navigation = navigationHandle<DetailKey>()
    TitledColumn("Details") {
        Text("id: ${navigation.key.id}")
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.completeFrom(DetailKey("->"+navigation.key.id))
        }) {
            Text("Complete from detail")
        }
        Button(onClick = {
            navigation.completeFrom(ResultKey())
        }) {
            Text("Complete from result")
        }
    }
}

@Serializable
class ResultKey : NavigationKey.WithResult<String>

val resultDestination = navigationDestination<ResultKey> {
    val navigation = navigationHandle<ResultKey>()

    TitledColumn("Results") {
        Button(onClick = {
            navigation.complete("A")
        }) {
            Text("A")
        }
        Button(onClick = {
            navigation.complete("B")
        }) {
            Text("B")
        }

        Button(onClick = {
            navigation.complete("C")
        }) {
            Text("C")
        }

        Button(onClick = {
            navigation.completeFrom(ResultKey())
        }) {
            Text("Delegate")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}