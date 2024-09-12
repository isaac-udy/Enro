package dev.enro.tests.application.compose.results

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.result.registerForNavigationResult
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
object ComposeMixedResultTypes : NavigationKey.SupportsPush {
    @Parcelize
    internal class StringResult : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class IntResult : NavigationKey.SupportsPush.WithResult<Int>

    @Parcelize
    internal class ListOfStringResult : NavigationKey.SupportsPush.WithResult<List<String>>

    @Parcelize
    internal class BooleanResult : NavigationKey.SupportsPush.WithResult<Boolean>

    @Parcelize
    internal class AnotherObjectResult : NavigationKey.SupportsPush.WithResult<AnotherObject>
}

data class AnotherObject(
    val id: String = UUID.randomUUID().toString(),
    val int: Int = id.hashCode(),
)

class ComposeMixedResultViewModel : ViewModel() {

    var currentResult by mutableStateOf("No Result")

    val stringChannel by registerForNavigationResult<String> {
        currentResult = "${it::class.java.name} $it"
    }
    val intChannel by registerForNavigationResult<Int> {
        currentResult = "${it::class.java.name} $it"
    }
    val listOfStringChannel by registerForNavigationResult<List<String>> {
        currentResult = "${it::class.java.name} $it"
    }
    val booleanChannel by registerForNavigationResult<Boolean> {
        currentResult = "${it::class.java.name} $it"
    }
    val anotherObjectChannel by registerForNavigationResult<AnotherObject> {
        currentResult = "${it::class.java.name} $it"
    }
}

@NavigationDestination(ComposeMixedResultTypes::class)
@Composable
fun ComposeMixedResultTypesScreen() {
    val viewModel = viewModel<ComposeMixedResultViewModel>()

    TitledColumn(
        title = "Mixed Result Types",
    ) {
        Text("Current result: ${viewModel.currentResult}")

        Button(
            onClick = { viewModel.stringChannel.push(ComposeMixedResultTypes.StringResult()) }
        ) {
            Text("Get Result: String")
        }

        Button(
            onClick = { viewModel.intChannel.push(ComposeMixedResultTypes.IntResult()) }
        ) {
            Text("Get Result: Int")
        }

        Button(
            onClick = { viewModel.listOfStringChannel.push(ComposeMixedResultTypes.ListOfStringResult()) }
        ) {
            Text("Get Result: List<String>")
        }

        Button(
            onClick = { viewModel.booleanChannel.push(ComposeMixedResultTypes.BooleanResult()) }
        ) {
            Text("Get Result: Boolean")
        }

        Button(
            onClick = { viewModel.anotherObjectChannel.push(ComposeMixedResultTypes.AnotherObjectResult()) }
        ) {
            Text("Get Result: AnotherObject")
        }
    }
}

@NavigationDestination(ComposeMixedResultTypes.StringResult::class)
@Composable
fun StringResultScreen() {
    val navigation = navigationHandle<ComposeMixedResultTypes.StringResult>()
    TitledColumn("StringResult") {
        Button(
            onClick = { navigation.closeWithResult("\"This is a String\"") }
        ) {
            Text("Send Result")
        }
    }
}

@NavigationDestination(ComposeMixedResultTypes.IntResult::class)
@Composable
fun IntResultScreen() {
    val navigation = navigationHandle<ComposeMixedResultTypes.IntResult>()
    TitledColumn("IntResult") {
        Button(
            onClick = { navigation.closeWithResult(1) }
        ) {
            Text("Send Result")
        }
    }
}

@NavigationDestination(ComposeMixedResultTypes.ListOfStringResult::class)
@Composable
fun ListOfStringResultScreen() {
    val navigation = navigationHandle<ComposeMixedResultTypes.ListOfStringResult>()
    TitledColumn("ListOfStringResult") {
        Button(
            onClick = { navigation.closeWithResult(listOf("wow", "nice")) }
        ) {
            Text("Send Result")
        }
    }
}

@NavigationDestination(ComposeMixedResultTypes.BooleanResult::class)
@Composable
fun BooleanResultScreen() {
    val navigation = navigationHandle<ComposeMixedResultTypes.BooleanResult>()
    TitledColumn("BooleanResult") {
        Button(
            onClick = { navigation.closeWithResult(true) }
        ) {
            Text("Send Result")
        }
    }
}

@NavigationDestination(ComposeMixedResultTypes.AnotherObjectResult::class)
@Composable
fun AnotherObjectResultScreen() {
    val navigation = navigationHandle<ComposeMixedResultTypes.AnotherObjectResult>()
    TitledColumn("AnotherObjectResult") {
        Button(
            onClick = { navigation.closeWithResult(AnotherObject()) }
        ) {
            Text("Send Result")
        }
    }
}
