package nav.enro.example.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import nav.enro.core.NavigationHandle
import nav.enro.core.navigationHandle
import nav.enro.viewmodel.AbstractNavigationViewModelFactory
import java.lang.IllegalStateException
import java.lang.reflect.Constructor

abstract class SingleStateViewModel<T : Any>(): ViewModel() {
    private val internalState = MutableLiveData<T>()
    val observableState = internalState as LiveData<T>
    var state get() = internalState.value!!
        protected set(value) {
            internalState.value = value
        }
}