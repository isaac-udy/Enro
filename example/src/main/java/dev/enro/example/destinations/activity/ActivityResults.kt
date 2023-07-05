package dev.enro.example.destinations.activity

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.ExperimentalEnroApi
import dev.enro.core.NavigationKey
import dev.enro.core.activity.activityResultDestination
import dev.enro.core.activity.withInput
import dev.enro.core.activity.withMappedResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.present
import dev.enro.example.destinations.synthetic.SimpleMessage
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityResultExample : NavigationKey.SupportsPush

@Composable
@NavigationDestination(ActivityResultExample::class)
fun ActivityResultExampleScreen() {
    val navigation = navigationHandle()
    val getMediaName = registerForNavigationResult<String>(
        onClosed = {
            navigation.present(
                SimpleMessage(
                    title = "Activity Result",
                    message = "GetMediaFileName closed without a result"
                )
            )
        },
        onResult = {
            navigation.present(
                SimpleMessage(
                    title = "Activity Result",
                    message = "GetMediaFileName returned a result of: $it"
                )
            )
        }
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { getMediaName.present(GetVisualMediaFileName(false)) }) {
            Text("Get Media File Name")
        }
    }
}

@Parcelize
class GetVisualMediaFileName(
    val imageOnly: Boolean
) : NavigationKey.SupportsPresent.WithResult<String>

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(GetVisualMediaFileName::class)
val pickFileDestination = activityResultDestination(GetVisualMediaFileName::class) {
    ActivityResultContracts.PickVisualMedia()
        .withInput(
            PickVisualMediaRequest(
                when (key.imageOnly) {
                    true -> ActivityResultContracts.PickVisualMedia.ImageOnly
                    else -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
                }

            )
        )
        .withMappedResult {
            it.lastPathSegment ?: "unknown!"
        }
}