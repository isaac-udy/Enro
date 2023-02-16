package dev.enro.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.core.NavigationInstruction
import dev.enro.core.compose.navigationHandle

class SelectNavigationInstructionState {
    var instructions: List<Pair<String, NavigationInstruction>> by mutableStateOf(emptyList())
        private set

    var isVisible by mutableStateOf(false)
        private set

    fun dismiss() {
        isVisible = false
    }

    fun show(
        vararg instructions: Pair<String, NavigationInstruction>
    ) = show(instructions.toList())

    fun show(
        instructions: List<Pair<String, NavigationInstruction>>
    ) {
        this.instructions = instructions
        isVisible = true
    }
}

@Composable
fun rememberSelectNavigationInstructionState(): SelectNavigationInstructionState {
    val state = remember {
        SelectNavigationInstructionState()
    }

    val navigation = navigationHandle()
    if (state.isVisible) {
        Dialog(
            onDismissRequest = { state.dismiss() }
        ) {
            Card {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    state.instructions.forEach {
                        Text(
                            text = it.first,
                            modifier = Modifier
                                .clickable {
                                    state.dismiss()
                                    navigation.executeInstruction(it.second)
                                }
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
    return state
}