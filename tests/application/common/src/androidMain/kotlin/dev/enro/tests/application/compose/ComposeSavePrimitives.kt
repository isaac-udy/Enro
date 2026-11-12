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
import dev.enro.core.container.setBackstack
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDisplay
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

/**
 * Checks that a container restores a `rememberSaveable` list of primitives.
 * Restoring a saved list of primitives failed on some OS versions (API 34).
 *
 * To check manually, open this screen, note the numbers displayed, then either:
 * - with "Don't keep activities" enabled in the developer options, background
 *   the app and open the app again; or
 * - click "Save State", then click "Restore State".
 *
 * The screen must display the same numbers afterwards.
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
                // The container state is marshalled to a byte array and read back into a
                // new Bundle. A Bundle that is not marshalled can retain object references,
                // and retained references hide restoration failures.
                val parcel = Parcel.obtain()
                container.saveState().writeToParcel(parcel, 0)
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
                container.restoreState(bundle!!)
            }) {
                Text("Restore State")
            }
        }
        NavigationDisplay(container)
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
