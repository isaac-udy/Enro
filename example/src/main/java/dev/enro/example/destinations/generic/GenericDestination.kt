package dev.enro.example.destinations.generic

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.destination.compose.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class GenericDestination<T: Parcelable>(
    val instantResult: T
) : NavigationKey.SupportsPresent.WithResult<T>

@Composable
@NavigationDestination(GenericDestination::class)
fun GenericDestinationImplementation() {
    val navigation = navigationHandle<GenericDestination<Parcelable>>()
    LaunchedEffect(Unit) {
        navigation.closeWithResult(navigation.key.instantResult)
    }
}