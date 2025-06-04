package dev.enro.tests.application.compose

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.ui.LocalNavigationContainer
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

@Parcelize
object FindContext : Parcelable, NavigationKey.SupportsPush {
    interface HasId {
        val id: Int
    }

    @Parcelize
    internal object Left : Parcelable, NavigationKey.SupportsPush {
        @Parcelize
        internal data class Top(override val id: Int) : Parcelable, NavigationKey.SupportsPush, HasId

        @Parcelize
        internal data class Bottom(override val id: Int) : Parcelable, NavigationKey.SupportsPush, HasId
    }

    @Parcelize
    internal object Right : Parcelable, NavigationKey.SupportsPush {
        @Parcelize
        internal data class Top(override val id: Int) : Parcelable, NavigationKey.SupportsPush, HasId

        @Parcelize
        internal data class Bottom(override val id: Int) : Parcelable, NavigationKey.SupportsPush, HasId
    }

    @Parcelize
    internal object Find : Parcelable, NavigationKey.SupportsPresent

    @Parcelize
    internal class FindResult(val found: NavigationKey) : Parcelable, NavigationKey.SupportsPresent
}

@NavigationDestination(FindContext::class)
@Composable
fun FindContextDestination() {
    val navigation = navigationHandle()
    val left = rememberNavigationContainer(
        root = FindContext.Left,
        emptyBehavior = EmptyBehavior.CloseParent
    )
    val right = rememberNavigationContainer(
        root = FindContext.Right,
        emptyBehavior = EmptyBehavior.CloseParent
    )
    Box {
        Row {
            Box(modifier = Modifier.weight(1f)) {
                left.Render()
            }
            Box(modifier = Modifier.weight(1f)) {
                right.Render()
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            backgroundColor = MaterialTheme.colors.primary,
            onClick = { navigation.present(FindContext.Find) }
        ) {
            Text(text = "Find")
        }
    }
}

@NavigationDestination(FindContext.Left::class)
@Composable
fun FindContextLeftDestination() {
    val top = rememberNavigationContainer(
        root = FindContext.Left.Top(1),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<FindContext.Left.Top>() },
    )
    val bottom = rememberNavigationContainer(
        root = FindContext.Left.Bottom(1),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<FindContext.Left.Bottom>() }
    )
    Column {
        Box(modifier = Modifier.weight(1f)) {
            top.Render()
        }
        Box(modifier = Modifier.weight(1f)) {
            bottom.Render()
        }
    }
}

@NavigationDestination(FindContext.Right::class)
@Composable
fun FindContextRightDestination() {
    val top = rememberNavigationContainer(
        root = FindContext.Right.Top(1),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<FindContext.Right.Top>() }
    )
    val bottom = rememberNavigationContainer(
        root = FindContext.Right.Bottom(1),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<FindContext.Right.Bottom>() }
    )
    Column {
        Box(modifier = Modifier.weight(1f)) {
            top.Render()
        }
        Box(modifier = Modifier.weight(1f)) {
            bottom.Render()
        }
    }
}

@NavigationDestination(FindContext.Left.Top::class)
@Composable
fun FindContextLeftTopDestination() {
    val navigation = navigationHandle<FindContext.Left.Top>()
    LeafDestination(
        color = Color.Red,
        nextKey = FindContext.Left.Top(navigation.key.id + 1),
    )
}

@NavigationDestination(FindContext.Left.Bottom::class)
@Composable
fun FindContextLeftBottomDestination() {
    val navigation = navigationHandle<FindContext.Left.Bottom>()
    LeafDestination(
        color = Color.Blue,
        nextKey = FindContext.Left.Bottom(navigation.key.id + 1),
    )
}

@NavigationDestination(FindContext.Right.Top::class)
@Composable
fun FindContextRightTopDestination() {
    val navigation = navigationHandle<FindContext.Right.Top>()
    LeafDestination(
        color = Color.Green,
        nextKey = FindContext.Right.Top(navigation.key.id + 1),
    )
}

@NavigationDestination(FindContext.Right.Bottom::class)
@Composable
fun FindContextRightBottomDestination() {
    val navigation = navigationHandle<FindContext.Right.Bottom>()
    LeafDestination(
        color = Color.Yellow,
        nextKey = FindContext.Right.Bottom(navigation.key.id + 1),
    )
}

@Composable
fun LeafDestination(
    color: Color,
    nextKey: NavigationKey.SupportsPush,
) {
    val container = LocalNavigationContainer.current
    val navigation = navigationHandle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val title = "${if (navigation.key::class.java.name.contains("Left")) "Left" else "Right"}.${navigation.key}"
        val tagName = title.takeWhile { it != '(' }
        Text(
            text = title,
            style = MaterialTheme.typography.caption,
        )

        Button(onClick = {
            TODO("SET ACTIVE CONTAINER")
//            container?.setActive()
        }) {
            Text(
                modifier = Modifier.testTag("set-active-$tagName"),
                text = "Set Active",
            )
        }

        Button(onClick = {
            navigation.push(nextKey)
        }) {
            Text(
                modifier = Modifier.testTag("push-$tagName"),
                text = "Push",
            )
        }
    }
}

@NavigationDestination(FindContext.Find::class)
@Composable
fun FindContextDialog() {
    val navigation = navigationHandle()
//    val context = navigationContext
    var selectedType by remember { mutableStateOf<KClass<*>>(FindContext.Left.Top::class) }
    var selectedId by remember { mutableStateOf("") }
    DialogDestination {
        Dialog(onDismissRequest = { navigation.requestClose() }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextField(
                        modifier = Modifier.testTag("id-input"),
                        value = selectedId,
                        onValueChange = { selectedId = it },
                        placeholder = { Text(text = "(optional) id to search for") }
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = FindContext.Left.Top::class }
                    ) {
                        Text(text = "Find Left.Top")
                        Checkbox(
                            checked = selectedType == FindContext.Left.Top::class,
                            onCheckedChange = null,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = FindContext.Left.Bottom::class }
                    ) {
                        Text(text = "Find Left.Bottom")
                        Checkbox(
                            checked = selectedType == FindContext.Left.Bottom::class,
                            onCheckedChange = null
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = FindContext.Right.Top::class }
                    ) {
                        Text(text = "Find Right.Top")
                        Checkbox(
                            checked = selectedType == FindContext.Right.Top::class,
                            onCheckedChange = null
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = FindContext.Right.Bottom::class }
                    ) {
                        Text(text = "Find Right.Bottom")
                        Checkbox(
                            checked = selectedType == FindContext.Right.Bottom::class,
                            onCheckedChange = null
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            TODO("FIND")
//                            val id = selectedId.toIntOrNull()
//                            val foundContext = context.rootContext().findActiveContext {
//                                val key = it.instruction?.navigationKey as? FindContext.HasId
//                                key != null && key::class == selectedType && (id == null || key.id == id)
//                            }
//                            navigation.present(FindContext.FindResult(foundContext?.instruction?.navigationKey))
                        }
                    ) {
                        Text(text = "Find Active Context")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            TODO("FIND")
//                            val id = selectedId.toIntOrNull()
//                            val foundContext = context.rootContext().findContext {
//                                val key = it.instruction?.navigationKey as? FindContext.HasId
//                                key != null && key::class == selectedType && (id == null || key.id == id)
//                            }
//                            navigation.present(FindContext.FindResult(foundContext?.instruction?.navigationKey))
                        }
                    ) {
                        Text(text = "Find Context")
                    }
                }
            }
        }
    }
}

@NavigationDestination(FindContext.FindResult::class)
@Composable
fun FindContextResult() = DialogDestination {
    val navigation = navigationHandle<FindContext.FindResult>()
    val found = navigation.key.found
    Dialog(onDismissRequest = { navigation.requestClose() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                if (found == null) {
                    Text(text = "No context found")
                } else {
                    Text(text = "Found context: $found")
                }
            }
        }
    }
}
