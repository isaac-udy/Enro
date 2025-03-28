package dev.enro.example.destinations.listdetail.compose

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.example.core.data.Word
import dev.enro.example.core.data.typeName
import kotlinx.parcelize.Parcelize

@Parcelize
class DetailComposable(
    val word: Word
) : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(DetailComposable::class)
fun DetailComposeScreen() {
    val navigation = navigationHandle<DetailComposable>()
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = navigation.key.word.toString(),
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .padding(16.dp)
        )
        Text(
            text = navigation.key.word.typeName,
            style = MaterialTheme.typography.h5,
        )
    }
}
