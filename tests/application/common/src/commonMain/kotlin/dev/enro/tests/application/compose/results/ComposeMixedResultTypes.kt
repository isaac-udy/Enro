package dev.enro.tests.application.compose.results

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
object ComposeMixedResultTypes : NavigationKey {
    @Serializable
    internal class StringResult : NavigationKey.WithResult<String>

    @Serializable
    internal class IntResult : NavigationKey.WithResult<Int>

    @Serializable
    internal class ListOfStringResult : NavigationKey.WithResult<List<String>>

    @Serializable
    internal class BooleanResult : NavigationKey.WithResult<Boolean>

    @Serializable
    internal class AnotherObjectResult : NavigationKey.WithResult<AnotherObject>
}

data class AnotherObject(
    val id: String = Uuid.random().toString(),
    val int: Int = id.hashCode(),
)

class ComposeMixedResultViewModel : ViewModel() {

    var currentResult by mutableStateOf("No Result")

    val stringChannel by registerForNavigationResult<String> {
        currentResult = "${it::class.qualifiedName} $it"
    }
    val intChannel by registerForNavigationResult<Int> {
        currentResult = "${it::class.qualifiedName} $it"
    }
    val listOfStringChannel by registerForNavigationResult<List<String>> {
        currentResult = "${it::class.qualifiedName} $it"
    }
    val booleanChannel by registerForNavigationResult<Boolean> {
        currentResult = "${it::class.qualifiedName} $it"
    }
    val anotherObjectChannel by registerForNavigationResult<AnotherObject> {
        currentResult = "${it::class.qualifiedName} $it"
    }
}

@NavigationDestination(ComposeMixedResultTypes::class)
@Composable
fun ComposeMixedResultTypesScreen() {
    val viewModel = viewModel<ComposeMixedResultViewModel> {
        createEnroViewModel {
            ComposeMixedResultViewModel()
        }
    }

    TitledColumn(
        title = "Mixed Result Types",
    ) {
        Text("Current result: ${viewModel.currentResult}")

        Button(
            onClick = { viewModel.stringChannel.open(ComposeMixedResultTypes.StringResult()) }
        ) {
            Text("Get Result: String")
        }

        Button(
            onClick = { viewModel.intChannel.open(ComposeMixedResultTypes.IntResult()) }
        ) {
            Text("Get Result: Int")
        }

        Button(
            onClick = { viewModel.listOfStringChannel.open(ComposeMixedResultTypes.ListOfStringResult()) }
        ) {
            Text("Get Result: List<String>")
        }

        Button(
            onClick = { viewModel.booleanChannel.open(ComposeMixedResultTypes.BooleanResult()) }
        ) {
            Text("Get Result: Boolean")
        }

        Button(
            onClick = { viewModel.anotherObjectChannel.open(ComposeMixedResultTypes.AnotherObjectResult()) }
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
            onClick = { navigation.complete("\"This is a String\"") }
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
            onClick = { navigation.complete(1) }
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
            onClick = { navigation.complete(listOf("wow", "nice")) }
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
            onClick = { navigation.complete(true) }
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
            onClick = { navigation.complete(AnotherObject()) }
        ) {
            Text("Send Result")
        }
    }
}
