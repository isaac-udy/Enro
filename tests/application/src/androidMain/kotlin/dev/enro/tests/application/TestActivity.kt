package dev.enro.tests.application

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.content
import androidx.fragment.compose.rememberFragmentState
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro3.*
import dev.enro3.result.open
import dev.enro3.result.registerForNavigationResult
import dev.enro3.ui.NavigationDisplay
import dev.enro3.ui.destinations.syntheticDestination
import dev.enro3.ui.navigationDestination
import dev.enro3.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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

val listDestination = navigationDestination<ListKey> {
    val navigation = navigationHandle<NavigationKey>()
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
        Button(onClick = {
            navigation.open(SyntheticKey("Hello Synthetics"))
        }) {
            Text("Synthetic")
        }
        Button(onClick = {
            navigation.open(FragmentKey)
        }) {
            Text("Fragment")
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

@Serializable
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
            navigation.completeFrom(DetailKey("->" + navigation.key.id))
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

@Serializable
data class SyntheticKey(val message: String) : NavigationKey

val syntheticDestination = syntheticDestination<SyntheticKey> {
    Log.e("SyntheticKey", key.message)
}

@Serializable
object FragmentKey : NavigationKey

val fragmentDestination = navigationDestination<FragmentKey> {
    AndroidFragment<SimpleFragment>(
        fragmentState = rememberFragmentState(),
        arguments = Bundle().apply { putString("key", "value") }
    ) { fragment ->
    }
}

class SimpleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return content {
            val navigation = navigationHandle<FragmentKey>()
            TitledColumn("Simple Fragment") {
                Text("This is a simple fragment.")
                Button(onClick = { navigation.complete() }) {
                    Text("Complete")
                }
            }
        }
    }
}