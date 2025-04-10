package dev.enro.tests.application.activity

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.destination.compose.navigationContext
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object PictureInPicture : Parcelable, NavigationKey.SupportsPresent {
    @Parcelize
    class FirstChild : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    class SecondChild : Parcelable, NavigationKey.SupportsPush
}

@NavigationDestination(PictureInPicture::class)
class PictureInPictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
                root = PictureInPicture.FirstChild(),
                emptyBehavior = EmptyBehavior.CloseParent,
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                container.Render()
            }
        }
        applyInsetsForContentView()
    }
}

@NavigationDestination(PictureInPicture.FirstChild::class)
@Composable
fun PictureInPictureFirstChild() {
    val navigation = navigationHandle()
    val navigationContext = navigationContext
    TitledColumn(title = "Picture In Picture (First)") {
        Button(onClick = {
            navigation.push(PictureInPicture.SecondChild())
        }) {
            Text(text = "Push Second")
        }
        Button(onClick = {
            navigationContext.activity.safeEnterPictureInPictureMode()
        }) {
            Text(text = "Enter PiP")
        }
        Button(onClick = { navigation.requestClose() }) {
            Text(text = "Close")
        }
    }
}

@NavigationDestination(PictureInPicture.SecondChild::class)
@Composable
fun PictureInPictureSecondChild() {
    val navigation = navigationHandle()
    val navigationContext = navigationContext
    TitledColumn(title = "Picture In Picture (Second)") {
        Button(onClick = {
            navigationContext.activity.safeEnterPictureInPictureMode()
        }) {
            Text(text = "Enter PiP")
        }
        Button(onClick = { navigation.requestClose() }) {
            Text(text = "Close")
        }
    }
}

private fun ComponentActivity.safeEnterPictureInPictureMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        enterPictureInPictureMode(
            PictureInPictureParams.Builder().build()
        )
    }
}