@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.CreateResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@Parcelize
private class ResultChannelTestKey : NavigationKey.SupportsPresent

/**
 * CreateResultChannel has two different methods which allow the creation of a result channel.
 * One method allows the onResult/onClosed lambdas to accept a NavigationKey, and one method
 * hides the NavigationKey from the lambda. This is the basis of the `key vs. no key` distinction
 * in these tests, as channels created through either method should be uniquely identifiable.
 */
class CreateResultChannelTest {
    @Rule
    @JvmField
    val enroTestRule = EnroTestRule()

    @Test
    fun resultChannelsAreUniquelyIdentifiable_keyVsNoKey() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val channelOne = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = { _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        val channelTwo = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { _ -> },
            onResult = { _, _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        assertNotEquals(channelOne.id, channelTwo.id)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiable_keyVsKey() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val channelOne = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { _ -> },
            onResult = { _, _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        val channelTwo = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { _ -> },
            onResult = { _, _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        assertNotEquals(channelOne.id, channelTwo.id)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiable_noKeyVsNoKey() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val channelOne = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = {  _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        val channelTwo = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = {  _ -> },
            additionalResultId = "",
        ) as ResultChannelImpl

        assertNotEquals(channelOne.id, channelTwo.id)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiable_keyWithRepeatedLambda() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val result: (NavigationKey, String) -> Unit = { _, _ -> }
        val channelOne = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = result,
            additionalResultId = "",
        ) as ResultChannelImpl

        val channelTwo = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = result,
            additionalResultId = "",
        ) as ResultChannelImpl

        assertNotEquals(channelOne.id, channelTwo.id)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiable_noKeyWithRepeatedLambda() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val result: (String) -> Unit = {}
        val channelOne = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = result,
            additionalResultId = "",
        ) as ResultChannelImpl

        val channelTwo = createResultChannel.invoke(
            resultType = String::class,
            onClosed = { },
            onResult = result,
            additionalResultId = "",
        ) as ResultChannelImpl

        assertNotEquals(channelOne.id, channelTwo.id)
    }

    @Test
    fun resultChannelsAreNotUniquelyIdentifiableInForLoop() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val result: (String) -> Unit = {}
        val channels = (0..2).map {
            createResultChannel.invoke(
                resultType = String::class,
                onClosed = { },
                onResult = result,
                additionalResultId = "",
            ) as ResultChannelImpl
        }
        assertEquals(channels[0].id, channels[1].id)
    }

    @Test
    fun resultChannelsAreUniquelyIdentifiableInForLoop_providedAdditionalId() {
        val createResultChannel = createTestNavigationHandle(ResultChannelTestKey())
            .dependencyScope
            .get<CreateResultChannel>()

        val result: (String) -> Unit = {}
        val channels = (0..2).map {
            createResultChannel.invoke(
                resultType = String::class,
                onClosed = { },
                onResult = result,
                additionalResultId = "loop@$it",
            ) as ResultChannelImpl
        }
        assertNotEquals(channels[0].id, channels[1].id)
    }
}