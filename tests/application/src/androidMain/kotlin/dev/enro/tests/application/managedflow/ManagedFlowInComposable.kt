package dev.enro.tests.application.managedflow

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.rememberNavigationContainer
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
    val navigation = navigationHandle<ManagedFlowInComposable>()
    val container = rememberNavigationContainer(
        backstack = listOf(UserInformationFlow().asInstance()),
        interceptor = navigationInterceptor {
            onCompleted<UserInformationFlow> {
                cancelAnd {
                    navigation.open(ManagedFlowInComposable.DisplayUserInformation(result))
                }
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