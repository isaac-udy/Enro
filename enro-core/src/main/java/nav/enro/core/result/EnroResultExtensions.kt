package nav.enro.core.result

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationKey
import nav.enro.core.TypedNavigationHandle
import nav.enro.core.close
import nav.enro.core.result.internal.LazyResultChannelProperty
import nav.enro.core.result.internal.PendingResult
import nav.enro.core.result.internal.ResultChannelImpl
import kotlin.properties.ReadOnlyProperty


fun <T: Any> NavigationHandle.closeWithResult(result: T) {
    val resultId = ResultChannelImpl.getResultId(this)
    if(resultId != null) {
        EnroResult.from(controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
    close()
}

fun <T: Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    val resultId = ResultChannelImpl.getResultId(this)
    if(resultId != null) {
        EnroResult.from(controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
    close()
}

inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    navigationHandle: NavigationHandle,
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = navigationHandle,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> FragmentActivity.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<FragmentActivity, EnroResultChannel<T>>  =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> Fragment.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T>>  =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
        onResult = onResult
    )