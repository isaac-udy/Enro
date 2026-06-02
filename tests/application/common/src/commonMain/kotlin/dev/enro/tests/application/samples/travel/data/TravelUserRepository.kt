package dev.enro.tests.application.samples.travel.data

import dev.enro.tests.application.samples.travel.domain.TravelUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TravelUserRepository private constructor() {
    private val _registeredUsers = MutableStateFlow<List<TravelUser>>(emptyList())
    val registeredUsers: StateFlow<List<TravelUser>> = _registeredUsers.asStateFlow()

    private val _currentUser = MutableStateFlow<TravelUser?>(null)
    val currentUser: StateFlow<TravelUser?> = _currentUser.asStateFlow()

    fun registerUser(firstName: String, lastName: String, username: String, password: String): Boolean {
        // Check if username already exists
        if (_registeredUsers.value.any { it.username == username }) {
            return false
        }

        val newUser = TravelUser(
            id = Uuid.random().toString(),
            name = "$firstName $lastName",
            username = username
        )

        _registeredUsers.value = _registeredUsers.value + newUser
        // Automatically log in the new user
        _currentUser.value = newUser
        return true
    }

    fun login(username: String, password: String): Boolean {
        // For this sample app, we'll accept any password as long as the username exists
        val user = _registeredUsers.value.find { it.username == username }
        return if (user != null) {
            _currentUser.value = user
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun isUsernameTaken(username: String): Boolean {
        return _registeredUsers.value.any { it.username == username }
    }

    fun getUserByUsername(username: String): TravelUser? {
        return _registeredUsers.value.find { it.username == username }
    }

    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    companion object {
        val instance = TravelUserRepository()
    }
}
