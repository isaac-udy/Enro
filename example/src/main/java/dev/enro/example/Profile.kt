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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberEnroContainerState
import dev.enro.core.forward
import kotlinx.parcelize.Parcelize

@Parcelize
class Profile : NavigationKey


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
                        EnroContainer(modifier = Modifier.fillMaxWidth().fillMaxHeight(), state = rememberEnroContainerState {
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

@Composable
@NavigationDestination(InitialKey::class)
fun InitialScreen() {
    Column {
        val navigation = navigationHandle()
        Button(onClick = { navigation.forward(NestedKey()) }) {
            Text(text = "Open Nested!")
        }
        Button(onClick = { navigation.forward(NestedKey2()) }) {
            Text(text = "Open Nested 2!")
        }
        EnroContainer(modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, Color.Green), state = rememberEnroContainerState() { it is NestedKey })
        EnroContainer(modifier =  Modifier.fillMaxWidth().height(200.dp).border(1.dp, Color.Red), state = rememberEnroContainerState() { it is NestedKey2 })
    }
}

@Parcelize
class NestedKey : NavigationKey

@Composable
@NavigationDestination(NestedKey::class)
fun NestedScreen() {
    Column {
        Text("NESTED ONE!")
    }
}

@Parcelize
class NestedKey2 : NavigationKey

@Composable
@NavigationDestination(NestedKey2::class)
fun NestedScreen2() {
    Column {
        Text("NESTED TWO!")
    }
}

