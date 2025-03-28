package dev.enro.tests.application.managedflow

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object ManagedFlowInComposable : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal class DisplayUserInformation(
        val userInformation: UserInformation,
    ) : Parcelable, NavigationKey.SupportsPush
}

@Composable
@NavigationDestination(ManagedFlowInComposable::class)
fun ManagedFlowInComposableScreen() {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
        root = UserInformationFlow(),
        filter = accept {
            key<UserInformationFlow>()
            key<ManagedFlowInComposable.DisplayUserInformation>()
        },
        interceptor = {
            onResult<UserInformationFlow, UserInformation> { _, result ->
                replaceCloseWith(
                    ManagedFlowInComposable.DisplayUserInformation(result).asPush()
                )
            }
        }
    )
    Box(modifier = Modifier.fillMaxSize()) {
        container.Render()
    }
}

@NavigationDestination(ManagedFlowInComposable.DisplayUserInformation::class)
@Composable
fun DisplayUserInformationScreen() {
    val navigation = navigationHandle<ManagedFlowInComposable.DisplayUserInformation>()
    TitledColumn(
        title = "User Information",
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Name: ${navigation.key.userInformation.name}")
        Text("Email: ${navigation.key.userInformation.email}")
        Text("Age: ${navigation.key.userInformation.age}")
    }
}