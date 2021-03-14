package dev.enro.core.result

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.close
import dev.enro.core.result.internal.LazyResultChannelProperty
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelImpl
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


inline fun <reified T : Any> ViewModel.forwardNavigationResult(
        navigationHandle: TypedNavigationHandle<NavigationKey.WithResult<T>>
): ReadOnlyProperty<Any, EnroResultChannel<T>> =
        LazyResultChannelProperty(
                owner = navigationHandle,
                resultType = T::class.java,
                onResult = {
                    navigationHandle.closeWithResult(it)
                }
        )

inline fun <reified T : Any> FragmentActivity.forwardNavigationResult(
        navigationHandle: TypedNavigationHandle<NavigationKey.WithResult<T>>
): ReadOnlyProperty<FragmentActivity, EnroResultChannel<T>>  =
        LazyResultChannelProperty(
                owner = this,
                resultType = T::class.java,
                onResult = {
                    navigationHandle.closeWithResult(it)
                }
        )

inline fun <reified T : Any> Fragment.forwardNavigationResult(
        crossinline navigationHandle: () -> TypedNavigationHandle<out NavigationKey.WithResult<T>>
): ReadOnlyProperty<Fragment, EnroResultChannel<T>>  =
        LazyResultChannelProperty(
                owner = this,
                resultType = T::class.java,
                onResult = {
                    navigationHandle().closeWithResult(it)
                }
        )