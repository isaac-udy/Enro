package dev.enro.core.container

import android.os.Parcelable
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class NavigationInstructionFilterBuilderTests {

    @Test
    fun acceptAll_() {
        val filter = acceptAll()
        listOf(
            TestKeys.One() to true,
            TestKeys.Two() to true,
            TestKeys.Three() to true,
            TestKeys.Four() to true,
            TestKeys.Five() to true,
            TestKeys.ObjectKeyOne to true,
            TestKeys.ObjectKeyTwo to true,
        )
            .map {
                it.first.asPush() to it.second
            }
            .forEach {
                assertEquals(
                    it.second,
                    filter.accept(it.first),
                )
            }

    }

    @Test
    fun acceptNone_() {
        val filter = acceptNone()
        listOf(
            TestKeys.One() to false,
            TestKeys.Two() to false,
            TestKeys.Three() to false,
            TestKeys.Four() to false,
            TestKeys.Five() to false,
            TestKeys.ObjectKeyOne to false,
            TestKeys.ObjectKeyTwo to false,
        )
            .map {
                it.first.asPush() to it.second
            }
            .forEach {
                assertEquals(
                    it.second,
                    filter.accept(it.first),
                )
            }

    }

    @Test
    fun acceptSpecificKey() {
        val filter = accept {
            key { it is TestKeys.Two }
            key { it is TestKeys.Three && it.parameter == "three" }
            key<TestKeys.Four>()
            key(TestKeys.Five("a"))
        }
        listOf(
            TestKeys.One() to false,
            TestKeys.Two() to true,
            TestKeys.Three() to false,
            TestKeys.Three("three") to true,
            TestKeys.Four() to true,
            TestKeys.Five("a") to true,
            TestKeys.ObjectKeyOne to false,
            TestKeys.ObjectKeyTwo to false,
        )
            .map {
                it.first.asPush() to it.second
            }
            .forEach {
                assertEquals(
                    "Failed for ${it.first.navigationKey}",
                    it.second,
                    filter.accept(it.first),
                )
            }

    }

    @Test
    fun doNotAcceptSpecificKey() {
        val filter = doNotAccept {
            key { it is TestKeys.Two }
            key { it is TestKeys.Three && it.parameter == "three" }
            key<TestKeys.Four>()
            key(TestKeys.Five("a"))
        }
        listOf(
            TestKeys.One() to true,
            TestKeys.Two() to false,
            TestKeys.Three() to true,
            TestKeys.Three("three") to false,
            TestKeys.Four() to false,
            TestKeys.Five("a") to false,
            TestKeys.ObjectKeyOne to true,
            TestKeys.ObjectKeyTwo to true,
        )
            .map {
                it.first.asPush() to it.second
            }
            .forEach {
                assertEquals(
                    "Failed for ${it.first.navigationKey}",
                    it.second,
                    filter.accept(it.first),
                )
            }

    }

    object TestKeys {
        @Parcelize
        data class One(val parameter: String = UUID.randomUUID().toString()) :
            NavigationKey.SupportsPush,
            NavigationKey.SupportsPresent

        @Parcelize
        data class Two(val parameter: String = UUID.randomUUID().toString()) :
            NavigationKey.SupportsPush,
            NavigationKey.SupportsPresent

        @Parcelize
        data class Three(val parameter: String = UUID.randomUUID().toString()) :
            NavigationKey.SupportsPush,
            NavigationKey.SupportsPresent

        @Parcelize
        data class Four(val parameter: String = UUID.randomUUID().toString()) :
            NavigationKey.SupportsPush,
            NavigationKey.SupportsPresent

        @Parcelize
        data class Five(val parameter: String = UUID.randomUUID().toString()) :
            NavigationKey.SupportsPush,
            NavigationKey.SupportsPresent

        @Parcelize
        data object ObjectKeyOne : Parcelable, NavigationKey.SupportsPush, NavigationKey.SupportsPresent

        @Parcelize
        data object ObjectKeyTwo : Parcelable, NavigationKey.SupportsPush, NavigationKey.SupportsPresent
    }
}

