package dev.enro.example.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.example.core.data.Word
import dev.enro.example.core.data.typeName

@Composable
fun WordCard(
    word: Word,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .let {
                if(onClick != null) it.clickable { onClick() }
                else it
            }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = word.toString()
            )
            Text(
                text = word.typeName,
                style = MaterialTheme.typography.caption
            )
        }
    }
}