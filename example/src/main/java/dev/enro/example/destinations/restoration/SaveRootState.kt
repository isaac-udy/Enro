package dev.enro.example.destinations.restoration

import android.os.Parcelable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.requireRootContainer
import dev.enro.core.synthetic.syntheticDestination
import kotlinx.parcelize.Parcelize

@Parcelize
class SaveRootState : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(SaveRootState::class)
val saveRootState = syntheticDestination<SaveRootState> {
    val root = navigationContext.requireRootContainer().childContext?.containerManager?.activeContainer
        ?: return@syntheticDestination
    val savedState = root.save()
    root.setBackstack { emptyBackstack().push(WaitForRestoration(savedState)) }
}