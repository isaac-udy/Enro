package dev.enro.hilt.test

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.enro.DefaultActivity
import dev.enro.TestActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.destination.compose.EnroContainer
import dev.enro.expectContext
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import dev.enro.waitOnMain
import junit.framework.TestCase.assertTrue
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton


@HiltAndroidTest
class HiltViewModelCreationTests {

    @get:Rule
    val hilt = HiltAndroidRule(this)

    @Test
    fun whenActivityFragmentComposable_requestHiltInjectedViewModels_thenViewModelsAreCreated() {
        ActivityScenario.launch(DefaultActivity::class.java)

        expectContext<DefaultActivity, NavigationKey>()
            .navigation
            .forward(ContainerActivity.Key())

        expectContext<ContainerActivity, ContainerActivity.Key>()
            .apply {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    context.viewModel.hashCode()
                }
            }
            .navigation
            .forward(ContainerFragment.Key())

        val fragment = expectContext<ContainerFragment, ContainerFragment.Key>()

        fragment
            .apply {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    context.viewModel.hashCode()
                }
            }
            .navigation
            .forward(Compose.Key())

        // TODO: Once Enro 2.0 is released, this hacky way of checking the current top composable can be removed
        val activeNavigation = waitOnMain {
            fragment.context.containerManager.activeContainer?.childContext?.getNavigationHandle()
        }
        Thread.sleep(1000)
        assertTrue(activeNavigation.key is Compose.Key)
    }

    @AndroidEntryPoint
    @NavigationDestination(ContainerActivity.Key::class)
    class ContainerActivity : TestActivity() {

        val viewModel by enroViewModels<TestViewModel>()
        private val navigation by navigationHandle<Key> {
            container(primaryFragmentContainer) {
                it is ContainerFragment.Key
            }
        }

        @Parcelize
        class Key : NavigationKey

        @HiltViewModel
        class TestViewModel @Inject constructor(
            val useCaseOne: ExampleDependencies.UseCaseOne,
            val useCaseTwo: ExampleDependencies.UseCaseTwo,
            val application: Application,
            val savedStateHandle: SavedStateHandle
        ): ViewModel() {
            val navigation by navigationHandle<Key>()
        }
    }

    @AndroidEntryPoint
    @NavigationDestination(ContainerFragment.Key::class)
    class ContainerFragment : androidx.fragment.app.Fragment() {

        val viewModel by enroViewModels<TestViewModel>()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return ComposeView(requireContext()).apply {
                setContent {
                    EnroContainer(
                        modifier = Modifier.size(width = 200.dp, height = 200.dp)
                    )
                }
            }
        }

        @Parcelize
        class Key : NavigationKey

        @HiltViewModel
        class TestViewModel @Inject constructor(
            val useCaseOne: ExampleDependencies.UseCaseOne,
            val useCaseTwo: ExampleDependencies.UseCaseTwo,
            val application: Application,
            val savedStateHandle: SavedStateHandle
        ): ViewModel() {
            val navigation by navigationHandle<Key>()
        }
    }


    object Compose {
        @Composable
        @NavigationDestination(Key::class)
        fun Draw() {
            val viewModel = viewModel<TestViewModel>()

            Text("Text with ${viewModel.navigation.key}")
        }

        @Parcelize
        class Key : NavigationKey

        @HiltViewModel
        class TestViewModel @Inject constructor(
            val useCaseOne: ExampleDependencies.UseCaseOne,
            val useCaseTwo: ExampleDependencies.UseCaseTwo,
            val application: Application,
            val savedStateHandle: SavedStateHandle
        ): ViewModel() {
            val navigation by navigationHandle<Key>()
        }
    }
}

object ExampleDependencies {

    @Singleton
    class RepositoryOne @Inject constructor() {}

    @Singleton
    class RepositoryTwo @Inject constructor() {}

    class UseCaseOne @Inject constructor(
        val repositoryOne: RepositoryOne
    ) {}

    class UseCaseTwo @Inject constructor(
        val repositoryOne: RepositoryOne,
        val repositoryTwo: RepositoryTwo
    ) {}
}