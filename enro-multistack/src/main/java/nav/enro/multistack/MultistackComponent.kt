package nav.enro.multistack

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.NavigationKey
import nav.enro.core.context.parentActivity
import nav.enro.core.controller.NavigationControllerBuilder
import nav.enro.core.controller.createNavigationComponent
import nav.enro.core.executors.DefaultActivityExecutor

@Parcelize
data class MultiStackContainer(
    val containerId: Int,
    val rootKey: NavigationKey
) : Parcelable

inline fun <reified A : FragmentActivity> multiStackComponent(
    vararg containers: MultiStackContainer
): NavigationControllerBuilder =
    createNavigationComponent {
        override<Any, A>(
            launch = {
                it.fromContext.parentActivity.application.registerActivityLifecycleCallbacks(
                    AttachFragment(
                        A::class,
                        MultiStackControllerFragment().apply {
                            arguments = Bundle().apply {
                                putParcelableArray("containers", containers)
                            }
                        }
                    )
                )
                DefaultActivityExecutor.open(it)
            },
            close = {
                DefaultActivityExecutor.close(it)
            }
        )
    }