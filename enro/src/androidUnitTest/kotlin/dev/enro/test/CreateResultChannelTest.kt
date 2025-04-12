@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.CreateResultChannel
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.NavigationResultScope
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.result.registerForNavigationResult
import dev.enro.test.extensions.putNavigationHandleForViewModel
import dev.enro.viewmodel.withNavigationHandle
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@Parcelize
private class ResultChannelTestKey : Parcelable, NavigationKey.SupportsPresent


class CreateResultChannelTest {
    @Rule
    @JvmField
    val enroTestRule = EnroTestRule()

    @Test
    fun resultChannelsAreUniquelyIdentifiableWithinViewModel() {
        class ExampleOne : ViewModel() {
            val channelOne by registerForNavigationResult<String> {  }
            val channelTwo by registerForNavigationResult<String> {  }
        }
        putNavigationHandleForViewModel<ExampleOne>(ResultChannelTestKey())
        val viewModel = ExampleOne()
        assertNotEquals(viewModel.channelOne.internalId, viewModel.channelTwo.internalId)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiableWithinViewModel_keyWithRepeatedLambda() {
        createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val result: NavigationResultScope<String, NavigationKey.WithResult<String>>.(String) -> Unit = { }

        class ExampleOne : ViewModel() {
            val channelOne by registerForNavigationResult<String>(onResult = result)
            val channelTwo by registerForNavigationResult<String>(onResult = result)
        }
        putNavigationHandleForViewModel<ExampleOne>(ResultChannelTestKey())
        val viewModel = ExampleOne()
        assertNotEquals(viewModel.channelOne.internalId, viewModel.channelTwo.internalId)
    }

    @Test
    fun viewModelResultChannelsAreUnique() {
        val nh = putNavigationHandleForViewModel<TestResultIdsViewModel>(TestResultIdsNavigationKey())
        val viewModel = ViewModelProvider.NewInstanceFactory()
            .withNavigationHandle(nh)
            .create(TestResultIdsViewModel::class, CreationExtras.Empty)

        assertNotEquals(viewModel.stringOne.internalId, viewModel.stringTwo.internalId)
        assertNotEquals(viewModel.stringOne.internalId, viewModel.intOne.internalId)
        assertNotEquals(viewModel.stringOne.internalId, viewModel.intTwo.internalId)

        assertNotEquals(viewModel.stringTwo.internalId, viewModel.intOne.internalId)
        assertNotEquals(viewModel.stringTwo.internalId, viewModel.intTwo.internalId)

        assertNotEquals(viewModel.intOne.internalId, viewModel.intTwo.internalId)
    }

    @Test
    fun viewModelResultChannelsWithKeyAreUnique() {
        val nh = putNavigationHandleForViewModel<TestResultIdsWithKeyViewModel>(TestResultIdsNavigationKey())
        val viewModel = ViewModelProvider.NewInstanceFactory()
            .withNavigationHandle(nh)
            .create(TestResultIdsWithKeyViewModel::class, CreationExtras.Empty)

        assertNotEquals(viewModel.stringOne.internalId, viewModel.stringTwo.internalId)
        assertNotEquals(viewModel.stringOne.internalId, viewModel.intOne.internalId)
        assertNotEquals(viewModel.stringOne.internalId, viewModel.intTwo.internalId)

        assertNotEquals(viewModel.stringTwo.internalId, viewModel.intOne.internalId)
        assertNotEquals(viewModel.stringTwo.internalId, viewModel.intTwo.internalId)

        assertNotEquals(viewModel.intOne.internalId, viewModel.intTwo.internalId)
    }
}

@Parcelize
class TestResultIdsNavigationKey : Parcelable, NavigationKey.SupportsPresent

class TestResultIdsViewModel : ViewModel() {
    var stringOneResult: String? = null
    var stringTwoResult: String? = null
    var intOneResult: Int? = null
    var intTwoResult: Int? = null

    val stringOne by registerForNavigationResult<String> {
        stringOneResult = it
    }

    val stringTwo by registerForNavigationResult<String> {
        stringTwoResult = it
    }

    val intOne by registerForNavigationResult<Int> {
        intOneResult = it
    }

    val intTwo by registerForNavigationResult<Int> {
        intTwoResult = it
    }
}

class TestResultIdsWithKeyViewModel : ViewModel() {
    var stringOneResult: String? = null
    var stringTwoResult: String? = null
    var intOneResult: Int? = null
    var intTwoResult: Int? = null

    val stringOne by registerForNavigationResult<String> {
        stringOneResult = it
    }

    val stringTwo by registerForNavigationResult<String> {
        stringTwoResult = it
    }

    val intOne by registerForNavigationResult<Int> {
        intOneResult = it
    }

    val intTwo by registerForNavigationResult<Int> {
        intTwoResult = it
    }
}


private val NavigationResultChannel<*, *>.internalId: ResultChannelId
    get() {
        return (this as ResultChannelImpl).id
    }