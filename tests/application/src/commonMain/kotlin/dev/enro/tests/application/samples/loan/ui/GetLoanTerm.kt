package dev.enro.tests.application.samples.loan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.tests.application.samples.loan.GetLoanTerm

@NavigationDestination(GetLoanTerm::class)
@Composable
fun GetLoanTermScreen() {
    val navigation = navigationHandle<GetLoanTerm>()
    var months by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Loan Term",
            style = MaterialTheme.typography.headlineMedium
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = when (val m = months.toIntOrNull()) {
                null -> ""
                in 1..11 -> "Less than 1 year"
                12 -> "1 year"
                in 13..23 -> "1 year ${m % 12} months"
                else -> "${m / 12} years${if (m % 12 > 0) " ${m % 12} months" else ""}"
            },
            style = MaterialTheme.typography.bodyMedium,
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
}
