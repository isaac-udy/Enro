package dev.enro.example.core.base

import androidx.lifecycle.*

abstract class SingleStateViewModel<T : Any>(): ViewModel() {
    private val internalState = MutableLiveData<T>()
    val observableState = internalState as LiveData<T>
    var state get() = internalState.value!!
        protected set(value) {
            internalState.value = value
        }
}