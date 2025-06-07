package dev.enro.tests.application.managedflow

import android.os.Parcelable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.managedFlowDestination
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class UserInformation(
    val name: String,
    val email: String,
    val age: Int,
) : Parcelable

@Parcelize
internal class UserInformationFlow : Parcelable, NavigationKey.SupportsPush.WithResult<UserInformation> {
    @Parcelize
    internal class GetName : Parcelable, NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class GetEmail : Parcelable, NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class GetAge : Parcelable, NavigationKey.SupportsPush.WithResult<Int>

    @Parcelize
    internal class ErrorDialog(
        internal val message: String
    ) : Parcelable, NavigationKey.SupportsPresent
}

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(UserInformationFlow::class)
internal val userInformationFlow: NavigationDestinationProvider<UserInformationFlow> =
    managedFlowDestination<UserInformationFlow, UserInformation>(
        flow = {
            val name = open { UserInformationFlow.GetName() }
            val email = open { UserInformationFlow.GetEmail() }
            val age = open { UserInformationFlow.GetAge() }

            UserInformation(
                name = name,
                email = email,
                age = age
            )
        }
    )

@Composable
@NavigationDestination(UserInformationFlow.GetName::class)
internal fun GetNameScreen() {
    val navigation = navigationHandle<UserInformationFlow.GetName>()
    var text by rememberSaveable { mutableStateOf("") }

    fun onDone() {
        if (text.length < 3) {
            navigation.present(UserInformationFlow.ErrorDialog("Name must be at least 3 characters"))
            return
        }

        navigation.closeWithResult(text)
    }

    TitledColumn("What's your name?") {

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("UserInformationFlow.GetName.TextField"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            value = text,
            onValueChange = {
                text = it
            }
        )

        Button(
            onClick = { onDone() }
        ) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(UserInformationFlow.GetEmail::class)
internal fun GetEmailScreen() {
    val navigation = navigationHandle<UserInformationFlow.GetEmail>()
    var text by rememberSaveable { mutableStateOf("") }
    fun onDone() {
        if (text.count { it == '@' } != 1) {
            navigation.present(UserInformationFlow.ErrorDialog("Email must contain one @ symbol"))
            return
        }
        if (text.count { it.isWhitespace() } > 0) {
            navigation.present(UserInformationFlow.ErrorDialog("Email must not contain whitespace"))
            return
        }

        val beforeAt = text.split("@").first()
        if (beforeAt.isEmpty()) {
            navigation.present(UserInformationFlow.ErrorDialog("Email must contain characters before the @ symbol"))
            return
        }

        val afterAt = text.split("@").last()
        val afterAtSplit = afterAt.split(".")
        if (afterAtSplit.size < 2) {
            navigation.present(UserInformationFlow.ErrorDialog("Email must contain a '.' after the @ symbol"))
            return
        }
        if (afterAtSplit.any { it.isEmpty() }) {
            navigation.present(UserInformationFlow.ErrorDialog("After the @ symbol, there must be characters before and after each '.', and you cannot repeat '.' symbols"))
            return
        }

        navigation.closeWithResult(text)
    }

    TitledColumn("What's your email?") {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("UserInformationFlow.GetEmail.TextField"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            value = text,
            onValueChange = {
                text = it
            }
        )

        Button(
            onClick = { onDone() }
        ) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(UserInformationFlow.GetAge::class)
internal fun GetAgeScreen() {
    val navigation = navigationHandle<UserInformationFlow.GetAge>()
    var text by rememberSaveable { mutableStateOf("") }

    fun onDone() {
        val age = text.toIntOrNull()
        if (age == null) {
            navigation.present(UserInformationFlow.ErrorDialog("Age must be a number"))
            return
        }
        if (age < 0) {
            navigation.present(UserInformationFlow.ErrorDialog("Age must be a positive number"))
            return
        }
        if (age < 16) {
            navigation.present(UserInformationFlow.ErrorDialog("You should probably be 16 or older to use this app"))
            return
        }
        navigation.closeWithResult(age)
    }

    TitledColumn("How old are you?") {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("UserInformationFlow.GetAge.TextField"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            value = text,
            onValueChange = {
                text = it
            }
        )

        Button(
            onClick = { onDone() }
        ) {
            Text("Continue")
        }
    }
}

@NavigationDestination(UserInformationFlow.ErrorDialog::class)
internal val errorDialogScreen = navigationDestination<UserInformationFlow.ErrorDialog>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<UserInformationFlow.ErrorDialog>()
    AlertDialog(
        onDismissRequest = {
            navigation.close()
        },
        title = {
            Text("Error")
        },
        text = {
            Text(navigation.key.message)
        },
        confirmButton = {
            Button(
                modifier = Modifier.testTag("UserInformationFlow.ErrorDialog.OK"),
                onClick = {
                    navigation.close()
                }
            ) {
                Text("OK")
            }
        }
    )
}
