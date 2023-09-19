package dev.enro.example.destinations.activity

import android.Manifest
import android.os.Build
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
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.core.NavigationKey
import dev.enro.android.activityResultDestination
import dev.enro.android.withInput
import dev.enro.android.withMappedResult
import dev.enro.destination.compose.navigationHandle
import dev.enro.destination.compose.registerForNavigationResult
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
    val getCameraPermission = registerForNavigationResult<RequestCameraPermission.Result> {
        when(it) {
            RequestCameraPermission.Result.GRANTED -> navigation.present(
                SimpleMessage(
                    title = "Activity Result",
                    message = "Camera permission granted"
                )
            )
            RequestCameraPermission.Result.DENIED -> navigation.present(
                SimpleMessage(
                    title = "Activity Result",
                    message = "Camera permission denied"
                )
            )
            RequestCameraPermission.Result.DENIED_PERMANENTLY -> navigation.present(
                SimpleMessage(
                    title = "Activity Result",
                    message = "Camera permission denied forever"
                )
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { getMediaName.present(GetVisualMediaFileName(false)) }) {
            Text("Get Media File Name")
        }
        Button(onClick = { getCameraPermission.present(RequestCameraPermission()) }) {
            Text("Request Camera Permission")
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

@Parcelize
class RequestCameraPermission : NavigationKey.SupportsPresent.WithResult<RequestCameraPermission.Result> {
    enum class Result {
        GRANTED,
        DENIED,
        DENIED_PERMANENTLY,
    }
}

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(RequestCameraPermission::class)
val requestCameraPermission = activityResultDestination(RequestCameraPermission::class) {
    ActivityResultContracts.RequestPermission()
        .withInput(Manifest.permission.CAMERA)
        .withMappedResult { granted ->
            when {
                granted -> RequestCameraPermission.Result.GRANTED
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> RequestCameraPermission.Result.DENIED
                else -> RequestCameraPermission.Result.DENIED_PERMANENTLY
            }
        }
}