package nav.enro.masterdetail

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.addOpenInstruction
import nav.enro.core.context.fragment
import nav.enro.core.context.parentActivity
import nav.enro.core.controller.NavigationControllerBuilder
import nav.enro.core.controller.createNavigationComponent

inline fun <reified Activity: FragmentActivity, reified Master: Fragment, reified Detail: Fragment> masterDetailComponent(
    @IdRes masterContainer: Int,
    @IdRes detailContainer: Int
): NavigationControllerBuilder = createNavigationComponent {
    activityToFragmentOverride<Activity, Master>(
        launch = {
            val fragment =  it.fromContext.childFragmentManager.fragmentFactory.instantiate(
                Master::class.java.classLoader!!,
                Master::class.java.name
            ).addOpenInstruction(it.instruction)

            it.fromContext.childFragmentManager.beginTransaction()
                .replace(masterContainer, fragment)
                .setPrimaryNavigationFragment(fragment)
                .commitNow()
        },
        close = {
            it.parentActivity.finish()
        }
    )

    activityToFragmentOverride<Activity, Detail>(
        launch = {
            val fragment =  it.fromContext.childFragmentManager.fragmentFactory.instantiate(
                Detail::class.java.classLoader!!,
                Detail::class.java.name
            ).addOpenInstruction(it.instruction)

            it.fromContext.childFragmentManager.beginTransaction()
                .replace(detailContainer, fragment)
                .setPrimaryNavigationFragment(fragment)
                .commitNow()
        },
        close = { context ->
            context.fragment.parentFragmentManager.beginTransaction()
                .remove(context.fragment)
                .setPrimaryNavigationFragment(context.parentActivity.supportFragmentManager.findFragmentById(masterContainer))
                .commitNow()
        }
    )
}