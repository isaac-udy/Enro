package dev.enro.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.result.deliverResultFromPresent
import dev.enro.core.result.deliverResultFromPush
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleEmbeddedFlow : NavigationKey.SupportsPush.WithResult<ExampleEmbeddedFlow.Result> {

    @Parcelize
    class StepOne : NavigationKey.SupportsPush.WithResult<Result>

    @Parcelize
    class StepTwo(
        val firstChoice: String,
    ) : NavigationKey.SupportsPush.WithResult<Result>

    @Parcelize
    class StepThree(
        val firstChoice: String,
        val secondChoice: String,
    ) : NavigationKey.SupportsPush.WithResult<Result>

    @Parcelize
    class FinalStep(
        val firstChoice: String,
        val secondChoice: String,
        val thirdChoice: String,
    ) : NavigationKey.SupportsPresent.WithResult<Result>

    data class Result(
        val firstChoice: String,
        val secondChoice: String,
        val thirdChoice: String,
    )
}

@Composable
@NavigationDestination(ExampleEmbeddedFlow::class)
fun ExampleEmbeddedFlowScreen() {
    var result by remember { mutableStateOf<ExampleEmbeddedFlow.Result?>(null) }
    val navigation = navigationHandle<ExampleEmbeddedFlow>()
    val resultChannel = registerForNavigationResult<ExampleEmbeddedFlow.Result>(
        onClosed = {
            result = ExampleEmbeddedFlow.Result(
                firstChoice = "Closed",
                secondChoice = "Closed",
                thirdChoice = "Closed"
            )
        },
        onResult = {
            result = it
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Current Result: $result")

        Button(
            onClick = {
                resultChannel.push(ExampleEmbeddedFlow.StepOne())
            }
        ) {
            Text("Get Result")
        }
    }
}


@Composable
@NavigationDestination(ExampleEmbeddedFlow.StepOne::class)
fun StepOneScreen() {
    val navigation = navigationHandle<ExampleEmbeddedFlow.StepOne>()
    var input by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Step One")
        TextField(value = input, onValueChange = { input = it })

        Button(
            onClick = {
                navigation.deliverResultFromPush(
                    ExampleEmbeddedFlow.StepTwo(
                        firstChoice = input
                    )
                )
            }
        ) {
            Text("Continue")
        }
    }
}



@Composable
@NavigationDestination(ExampleEmbeddedFlow.StepTwo::class)
fun StepTwoScreen() {
    val navigation = navigationHandle<ExampleEmbeddedFlow.StepTwo>()
    var input by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Step One: ${navigation.key.firstChoice}")

        Text("Step Two")
        TextField(value = input, onValueChange = { input = it })


        Button(
            onClick = {
                navigation.deliverResultFromPush(
                    ExampleEmbeddedFlow.StepThree(
                        firstChoice = navigation.key.firstChoice,
                        secondChoice = input
                    )
                )
            }
        ) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(ExampleEmbeddedFlow.StepThree::class)
fun StepThreeScreen() {
    val navigation = navigationHandle<ExampleEmbeddedFlow.StepThree>()
    var input by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Step One: ${navigation.key.firstChoice}")
        Text(text = "Step Two: ${navigation.key.secondChoice}")

        Text("Step Three")
        TextField(value = input, onValueChange = { input = it })


        Button(
            onClick = {
                navigation.deliverResultFromPresent(
                    ExampleEmbeddedFlow.FinalStep(
                        firstChoice = navigation.key.firstChoice,
                        secondChoice = navigation.key.secondChoice,
                        thirdChoice = input
                    )
                )
            }
        ) {
            Text("Continue")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(ExampleEmbeddedFlow.FinalStep::class)
fun FinalStepScreen() = BottomSheetDestination(
    skipHalfExpanded = true
) { state ->
    val navigation = navigationHandle<ExampleEmbeddedFlow.FinalStep>()

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Step One: ${navigation.key.firstChoice}")
                Text(text = "Step Two: ${navigation.key.secondChoice}")
                Text(text = "Step Three: ${navigation.key.thirdChoice}")

                Button(
                    onClick = {
                        navigation.closeWithResult(
                            ExampleEmbeddedFlow.Result(
                                firstChoice = navigation.key.firstChoice,
                                secondChoice = navigation.key.secondChoice,
                                thirdChoice = navigation.key.thirdChoice
                            )
                        )
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    ) {}
}