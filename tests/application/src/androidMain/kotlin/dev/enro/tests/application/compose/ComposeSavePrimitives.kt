package dev.enro.tests.application.compose

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.emptyBackstack
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

/**
 * There appears to be a bug on some OS versions (namely Android 34) with Enro's implementation
 * of some state saving mechanisims in Composable screens specifically when saving lists of primitives.
 * This screen is used to test and verify that those bugs don't exist.
 *
 * To reproduce the original bug, there are two approaches:
 * First Approach:
 * 1. Set "Don't keep activities" in the developer options
 * 2. Open the app, navigate to this screen, and take note of the numbers displayed
 * 3. Background the app, and then open it again
 * 4. The numbers should be the same as they were before the app was backgrounded
 *
 * Second Approach:
 * 1. Open the app, navigate to this screen, and take note of the numbers displayed
 * 2. Click the "Save State" button
 * 3. Click the "Restore State" button
 * 4. The numbers should be the same as they were before the state was saved
 */
@Parcelize
object ComposeSavePrimitives : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal object Inner : Parcelable, NavigationKey.SupportsPush
}

@NavigationDestination(ComposeSavePrimitives::class)
@Composable
fun ComposeSavePrimitivesScreen() {
    val container = rememberNavigationContainer(
        root = ComposeSavePrimitives.Inner,
        emptyBehavior = EmptyBehavior.AllowEmpty,
    )
    val savedBundle = rememberSaveable { mutableStateOf<Bundle?>(null) }
    TitledColumn("Compose Save Primitives") {
        if (savedBundle.value == null) {
            Button(onClick = {
                // We're going to force the container state to be fully serialized into a byte array
                // and then back again into a bundle, so that the restoration is "real", as opposed
                // to when a bundle isn't actually saved, where some references can be retained
                val parcel = Parcel.obtain()
                container.save().writeToParcel(parcel, 0)
                val savedParcel = Parcel.obtain().apply {
                    unmarshall(parcel.marshall(), 0, parcel.dataSize())
                }
                savedParcel.setDataPosition(0)
                val savedState =
                    savedParcel.readBundle(ComposeSavePrimitives::class.java.classLoader)
                savedBundle.value = savedState
                container.setBackstack(emptyBackstack())
            }) {
                Text("Save State")
            }
        } else {
            Button(onClick = {
                val bundle = savedBundle.value
                savedBundle.value = null
                container.restore(bundle!!)
            }) {
                Text("Restore State")
            }
        }
        container.Render()
    }
}

@Composable
@NavigationDestination(ComposeSavePrimitives.Inner::class)
fun ComposeSavePrimitivesInnerScreen() {
    val ints = rememberSaveable {
        List(10) { Random.nextInt() }
    }
    val intText = remember(ints) {
        ints.joinToString("\n")
    }
    TitledColumn("Saved:") {
        Text("Saved Ints:")
        Text(
            text = intText,
            modifier = Modifier
                .padding(start = 4.dp)
                .testTag("ComposeSavePrimitivesInnerScreen.intText")
        )
    }
}
