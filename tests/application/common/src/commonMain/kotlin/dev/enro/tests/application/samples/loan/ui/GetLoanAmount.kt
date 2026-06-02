package dev.enro.tests.application.samples.loan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

@Serializable
object GetLoanAmount : NavigationKey.WithResult<Int>

@NavigationDestination(GetLoanAmount::class)
@Composable
fun GetLoanAmountScreen() {
    val navigation = navigationHandle<GetLoanAmount>()
    var amount by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "How much do you need?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Don't worry, we're just dreaming here - no credit checks!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Only allow numeric input
                if (newValue.all { it.isDigit() }) {
                    amount = newValue
                }
            },
            label = { Text("Amount ($)") },
            placeholder = { Text("e.g., 50000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    amount.toIntOrNull()?.let {
                        if (it > 0) navigation.complete(it)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                amount.toIntOrNull()?.let {
                    navigation.complete(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = amount.isNotEmpty() && amount.toIntOrNull() != null && amount.toInt() > 0
        ) {
            Text("Continue")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
