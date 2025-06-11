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
object GetLoanTerm : NavigationKey.WithResult<Int>

@NavigationDestination(GetLoanTerm::class)
@Composable
fun GetLoanTermScreen() {
    val navigation = navigationHandle<GetLoanTerm>()
    var months by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "How long to pay it back?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Take your time - we've got all the time in the world!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = months,
            onValueChange = { newValue ->
                // Only allow numeric input
                if (newValue.all { it.isDigit() }) {
                    months = newValue
                }
            },
            label = { Text("Term (months)") },
            placeholder = { Text("e.g., 360 for 30 years") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    months.toIntOrNull()?.let {
                        if (it > 0) navigation.complete(it)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Text(
            text = when (val m = months.toIntOrNull()) {
                null -> ""
                in 1..11 -> "That's less than a year - ambitious!"
                12 -> "Just 1 year"
                in 13..23 -> "1 year and ${m % 12} month${if (m % 12 != 1) "s" else ""}"
                in 24..359 -> "${m / 12} years${if (m % 12 > 0) " and ${m % 12} month${if (m % 12 != 1) "s" else ""}" else ""}"
                else -> "${m / 12} years - that's a long-term commitment!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                months.toIntOrNull()?.let {
                    navigation.complete(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = months.isNotEmpty() && months.toIntOrNull() != null && months.toInt() > 0
        ) {
            Text("Continue")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
