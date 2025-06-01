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
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
import dev.enro3.ui.scenes.DialogSceneStrategy
import dev.enro3.ui.scenes.DirectOverlaySceneStrategy
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
            resultChannel.open(DialogKey())
        }) {
            Text("Dialog")
        }
        Button(onClick = {
            resultChannel.open(DirectDialogKey())
        }) {
            Text("Direct Dialog")
        }
        Button(onClick = {
            resultChannel.open(DirectButtonKey())
        }) {
            Text("Direct Button")
        }
        Button(onClick = {
            resultChannel.open(DirectBottomSheetKey())
        }) {
            Text("Direct Bottom Sheet")
        }
        Button(onClick = {
            resultChannel.open(ComposeSharedElementTransitions.List())
        }) {
            Text("Shared Elements")
        }
        Button(onClick = {
            resultChannel.open(NestedKey())
        }) {
            Text("Nested")
        }
        Button(onClick = {
            stringResultChannel.open(ScreenWithViewModelKey())
        }) {
            Text("ViewModel")
        }
        repeat(3) {
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
    // we can grab composition locals here
    val compositionLocals = currentComposer.apply { currentCompositionLocalMap }

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

val activityDestination = navigationDestination<ActivityKey>(
    metadata = mapOf(
        DirectOverlaySceneStrategy.overlay(),
    ),
) {
    val context = rememberCompositionContext()
    context.effectCoroutineContext
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

@Serializable
class DialogKey(
    val title: String = "Dialog"
) : NavigationKey

val dialogDestination = navigationDestination<DialogKey>(
    metadata = mapOf(
        DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            )
        )
    )
) {
    val navigation = navigationHandle<DialogKey>()
    TitledColumn(
        title = navigation.key.title,
        modifier = Modifier
            .shadow(2.dp)
    ) {
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}

@Serializable
class DirectDialogKey : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
val directDialogDestination = navigationDestination<DirectDialogKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    AlertDialog(
        onDismissRequest = { navigation.close() },
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colors.background,
        title = {
            Text("Direct Dialog")
        },
        confirmButton = {
            Button(onClick = { navigation.complete() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { navigation.close() }) {
                Text("Dismiss")
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    )
}

@Serializable
class DirectButtonKey : NavigationKey

val directButtonDestination = navigationDestination<DirectButtonKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.onBackground.copy(alpha = 0.16f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}

@Serializable
class DirectBottomSheetKey : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
val directBottomSheetDestination = navigationDestination<DirectBottomSheetKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    ModalBottomSheet(
        containerColor = MaterialTheme.colors.background,
        onDismissRequest = { navigation.close() },
    ) {
        TitledColumn(
            title = "Direct Bottom Sheet",
            modifier = Modifier,
        ) {
            Text(
                text = "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                    "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                    "other things like that. This text really does not have any meaning at all. " +
                    "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                    "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                    "other things like that. This text really does not have any meaning at all."
            )
            Text(
                text = "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all. " +
                        "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all."
            )
            Button(
                onClick = { navigation.complete() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Complete")
            }
            Button(
                onClick = { navigation.close() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close")
            }
        }
    }
}


@Serializable
class EmptyKey : NavigationKey
val emptyDestination = navigationDestination<EmptyKey> {}

@Serializable
class NestedKey : NavigationKey

val nestedDestination = navigationDestination<NestedKey> {
    val container = rememberNavigationContainer(
        backstack = listOf(EmptyKey().asInstance()),
    )
    TitledColumn(
        title = "Nested",
    ) {
        Button(
            onClick = {
                container.execute(
                    NavigationOperation.open(
                        ListKey().asInstance()
                    )
                )
            }
        ) {
            Text("Push List")
        }
        NavigationDisplay(container)
    }
}



