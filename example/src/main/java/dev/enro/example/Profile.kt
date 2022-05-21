package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.forward
import dev.enro.core.result.closeWithResult
import dev.enro.core.result.registerForNavigationResult
import dev.enro.viewmodel.navigationHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize

@Parcelize
class Profile : NavigationKey


@Composable
fun ProgileFragment() {
    EnroExampleTheme {
        Text(text = "Open Nested!")
        Column {
            val navigation = navigationHandle()
            Text(text = "Open Nested!")
            Button(onClick = { navigation.forward(InitialKey()) }) {
                Text(text = "Open Initial")
            }
            EnroContainer(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(), controller = rememberNavigationContainer {
                it is InitialKey
            })
        }
    }
}

@NavigationDestination(Profile::class)
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply{
            setContent {
                EnroExampleTheme {
                    Text(text = "Open Nested!")
                    Column {
                        val navigation = navigationHandle()
                        Text(text = "Open Nested!")
                        Button(onClick = { navigation.forward(InitialKey()) }) {
                            Text(text = "Open Initial")
                        }
                        EnroContainer(modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(), controller = rememberNavigationContainer {
                            it is InitialKey
                        })
                    }
                }
            }
        }
    }
}

@Parcelize
class InitialKey : NavigationKey

class InitialScreenViewModel : ViewModel() {
    val navigation by navigationHandle<InitialKey>()
    val state = MutableStateFlow("None!")

    private val resultChannel by registerForNavigationResult<String> {
        state.value = it
    }

    fun goNestedOne() {
        resultChannel.open(NestedKey())
    }

    fun goNestedTwo() {
        resultChannel.open(NestedKey2())
    }
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(InitialKey::class)
fun InitialScreen() {
    val viewModel = viewModel<InitialScreenViewModel>()
    val state = viewModel.state.collectAsState()
    Column {
        Text(text = "Last result: ${state.value}")
        Button(onClick = { viewModel.goNestedOne() }) {
            Text(text = "Open Nested!")
        }
        Button(onClick = { viewModel.goNestedTwo() }) {
            Text(text = "Open Nested 2!")
        }
        EnroContainer(modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color.Green), controller = rememberNavigationContainer() { it is NestedKey })
        EnroContainer(modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color.Red), controller = rememberNavigationContainer() { it is NestedKey2 })
    }
}

@Parcelize
class NestedKey : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(NestedKey::class)
@ExperimentalComposableDestination
fun NestedScreen() {
    val navigation = navigationHandle<NestedKey>()
    val state = rememberSaveable { mutableStateOf("None") }
    val channel = registerForNavigationResult<String> {
        state.value = it
    }
    Column {
        Text("NESTED ONE! ${state.value}")
        Button(onClick = { navigation.closeWithResult("One") }) {
            Text(text = "CloseResult")
        }
        Button(onClick = { channel.open(NestedKey2()) }) {
            Text(text = "Open Nested2!")
        }
    }
}

@Parcelize
class NestedKey2 : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(NestedKey2::class)
@ExperimentalComposableDestination
fun NestedScreen2() {
    val navigation = navigationHandle<NestedKey2>()
    Column {
        Text("NESTED TWO!")
        Button(onClick = { navigation.closeWithResult("!") }) {
            Text(text = "CloseResult")
        }
    }
}

