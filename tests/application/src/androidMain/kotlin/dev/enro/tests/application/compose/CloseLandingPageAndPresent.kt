package dev.enro.tests.application.compose

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.tests.application.activity.applyInsetsForContentView
import kotlinx.parcelize.Parcelize

@Parcelize
object CloseLandingPageAndPresent : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(CloseLandingPageAndPresent::class)
class CloseRootAndPresentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
                root = LandingPageDestination(),
                emptyBehavior = EmptyBehavior.CloseParent,
            )
            container.Render()
        }
        applyInsetsForContentView()
    }
}

@Parcelize
internal class LandingPageDestination : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(LandingPageDestination::class)
fun LandingPageScreen() {
    val navigationHandle = navigationHandle()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(text = "Landing Page Screen", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigationHandle.push(InitialDestination())
            navigationHandle.close()
        }) {
            Text(text = "Continue")
        }
    }
}

@Parcelize
internal class InitialDestination : Parcelable, NavigationKey.SupportsPush

@Composable
@NavigationDestination(InitialDestination::class)
fun InitialScreen() {
    val navigationHandle = navigationHandle()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(text = "Initial Screen", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navigationHandle.present(PresentedBottomSheetDestination()) }) {
            Text(text = "Present Bottom Sheet")
        }
    }
}

@Parcelize
internal class PresentedBottomSheetDestination : Parcelable, NavigationKey.SupportsPresent

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(PresentedBottomSheetDestination::class)
fun BottomSheetScreen() = BottomSheetDestination { sheetState ->
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            val navigationHandle = navigationHandle()
            Column(Modifier.padding(16.dp)) {
                Text(text = "Bottom Sheet", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navigationHandle.close() }) {
                    Text(text = "Close")
                }
            }
        }
    ) {}
}