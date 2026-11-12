// Snippets for docs/ghpages/docs/core-concepts/navigation-keys.md
package dev.enro.docs.coreconcepts.navigationkeys

import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.open
import dev.enro.withMetadata
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Clock

// region docs:navigation-keys/show-profile
@Serializable
data class ShowProfile(val userId: String) : NavigationKey
// endregion

// region docs:navigation-keys/select-date
@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
// endregion

// region docs:navigation-keys/serializers-module
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(kotlinx.serialization.modules.SerializersModule {
            // contextual / polymorphic registrations
        })
    }
)
// endregion

// region docs:navigation-keys/with-result
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey.WithResult<Boolean>
// endregion

fun initialBackstack() {
    // region docs:navigation-keys/instance
    val initial = backstackOf(
        Home.asInstance(),
        ShowProfile("user-123").asInstance(),
    )
    // endregion
}

// region docs:navigation-keys/metadata-key
object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)
// endregion

fun openWithMetadata(navigation: NavigationHandle<*>) {
    // region docs:navigation-keys/with-metadata
    navigation.open(
        ShowProfile("user-123").withMetadata(OpenedAt, Clock.System.now().toEpochMilliseconds())
    )
    // endregion
}

// region docs:navigation-keys/data-object
@Serializable
data object Home : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
// endregion
