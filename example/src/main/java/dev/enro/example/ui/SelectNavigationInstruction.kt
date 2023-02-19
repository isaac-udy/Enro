package dev.enro.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.core.NavigationInstruction
import dev.enro.core.compose.navigationHandle
import java.util.*

class SelectNavigationInstructionState {
    var instructions: List<Pair<String, NavigationInstruction?>> by mutableStateOf(emptyList())
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
        instructions: List<Pair<String, NavigationInstruction?>>
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
                        .padding(bottom = 8.dp)
                ) {
                    state.instructions.forEach {
                        if (it.second == null) {
                            Text(
                                text = it.first,
                                style = MaterialTheme.typography.subtitle2,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 8.dp
                                    )
                            )
                        } else {
                            Text(
                                text = it.first,
                                modifier = Modifier
                                    .clickable {
                                        state.dismiss()
                                        val instruction = when (val instruction = it.second) {
                                            is NavigationInstruction.Open<*> -> (it.second as NavigationInstruction.Open<*>).copy(
                                                UUID.randomUUID().toString()
                                            )
                                            else -> instruction
                                        }
                                        instruction ?: return@clickable
                                        navigation.executeInstruction(instruction)
                                    }
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    return state
}