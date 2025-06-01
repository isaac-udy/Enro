package dev.enro.tests.application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.content
import androidx.fragment.compose.rememberFragmentState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro3.*
import dev.enro3.result.open
import dev.enro3.result.registerForNavigationResult
import dev.enro3.ui.NavigationDisplay
import dev.enro3.ui.destinations.syntheticDestination
import dev.enro3.ui.navigationDestination
import dev.enro3.ui.navigationHandle
import dev.enro3.ui.rememberNavigationContainer
import dev.enro3.viewmodel.createEnroViewModel
import dev.enro3.viewmodel.navigationHandle
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
        Button(onClick = {
            navigation.open(ActivityKey)
        }) {
            Text("Activity")
        }
        Button(onClick = {
            stringResultChannel.open(ScreenWithViewModelKey())
        }) {
            Text("ViewModel")
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

@Serializable
object ActivityKey : NavigationKey

val activityDestination = navigationDestination<ActivityKey> {
    val navigation = navigationHandle<ActivityKey>()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                navigation.complete()
            }
            else -> {
                navigation.close()
            }
        }
    }
    val localContext = LocalContext.current
    val intent = remember {
        Intent(localContext, SimpleActivity::class.java)
    }
    LaunchedEffect(Unit) {
        launcher.launch(intent)
    }
}

class SimpleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TitledColumn("Simple Activity") {
                Text("This is a simple fragment.")
                Button(onClick = { finish() }) {
                    Text("Complete")
                }
            }
        }
    }
}

@Serializable
class ScreenWithViewModelKey : NavigationKey.WithResult<String>

class ScreenWithViewModelViewModel : ViewModel() {
    private val navigation = navigationHandle<ScreenWithViewModelKey>()

    fun onComplete(result: String) {
        navigation.complete(result)
    }
}

val screenWithViewModelDestination = navigationDestination<ScreenWithViewModelKey> {
    val localOwner = LocalViewModelStoreOwner.current
    Log.e("Enro", "$localOwner, ${localOwner is HasDefaultViewModelProviderFactory}")
    val viewModel = viewModel<ScreenWithViewModelViewModel> {
        createEnroViewModel {
            ScreenWithViewModelViewModel()
        }
    }
    TitledColumn("Screen with ViewModel") {
        Button(onClick = { viewModel.onComplete("From ViewModel") }) {
            Text("Complete")
        }
    }
}
