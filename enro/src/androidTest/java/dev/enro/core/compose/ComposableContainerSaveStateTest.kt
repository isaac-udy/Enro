package dev.enro.core.compose

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ActivityScenario
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.container.*
import dev.enro.expectComposableContext
import dev.enro.expectContext
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class ComposableContainerSaveStateTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenSavingContainerStateToBundle_thenHierarchyIsRestoredCorrectly_andViewModelsAreClearedCorrectly() {
        val scenario = ActivityScenario.launch(SaveHierarchyActivity::class.java)
        val activity = expectContext<SaveHierarchyActivity, SavedHierarchyRootKey>()

        val instructionOneKey = SaveHierarchyKey()
        val instructionOne = NavigationInstruction.Push(instructionOneKey)
        val instruction2 = NavigationInstruction.Push(SaveHierarchyKey())
        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainer) {
            setBackstack { backstackOf(instructionOne) }
        }

        Thread.sleep(500)
        expectComposableContext<SaveHierarchyKey>()
            .navigation.onContainer(instructionOneKey.childContainerKey) {
                setBackstack { backstackOf(instruction2) }
            }

        Thread.sleep(4000)
        var savedState: Bundle = bundleOf()

        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainer) {
            this as ComposableNavigationContainer
            savedState = save()
            setBackstack { emptyBackstack() }
        }
        Thread.sleep(4000)

        activity.navigation.onContainer(SaveHierarchyActivity.primaryContainer) {
            this as ComposableNavigationContainer
            restore(savedState)
        }

        Thread.sleep(8000)
    }
}

@Parcelize
object SavedHierarchyRootKey: NavigationKey.SupportsPresent


@NavigationDestination(SavedHierarchyRootKey::class)
class SaveHierarchyActivity : AppCompatActivity() {
    val navigation by navigationHandle { defaultKey(SavedHierarchyRootKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(primaryContainer)
            Column {
                Text("SaveHierarchyActivity")
                Box {
                     container.Render()
                }
            }
        }
    }

    companion object {
        val primaryContainer = NavigationContainerKey.FromName("SaveHierarchyActivity.primaryContainer")
    }
}

@Parcelize
data class SaveHierarchyKey(
    val id: String = UUID.randomUUID().toString()
) : NavigationKey.SupportsPush {
    val childContainerKey get() = NavigationContainerKey.FromName(id)
}

class SavedStateHierarchyViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    init {
        Log.e("SSHRVM", "${savedStateHandle.keys()}")
    }
    val id: String = UUID.randomUUID().toString()
    val saveStateHandleId = savedStateHandle.getStateFlow("savedStateId", UUID.randomUUID().toString())

    override fun onCleared() {
        super.onCleared()
        Log.e("SSHRVM", "${savedStateHandle.keys()}")
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@NavigationDestination(SaveHierarchyKey::class)
fun SaveHierarchyScreen() {
    val navigation = navigationHandle<SaveHierarchyKey>()
    val savedState = rememberSaveable { UUID.randomUUID().toString() }
    val viewModel = viewModel<SavedStateHierarchyViewModel>()

    val childContainer = rememberNavigationContainer(navigation.key.childContainerKey)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(8.dp)
    ) {
        Text("navigationId: ${navigation.id}")
        Text("viewModelId: ${viewModel.id}")
        Text("savedStateHandleId: ${viewModel.saveStateHandleId.value}")
        Text("savedStateId: $savedState")
        childContainer.Render()
    }
}

