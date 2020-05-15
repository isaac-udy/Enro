package nav.enro.example.core.data

import android.app.Application
import android.content.Context

class UserRepository private constructor(private val application: Application) {

    private val preferences = application.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)

    var activeUser: String?
        get() {
            return preferences.getString(KEY_USER_NAME, null)
        }
        set(value) {
            if(value == null) {
                preferences.edit().remove(KEY_USER_NAME).apply()
            } else {
                preferences.edit().putString(KEY_USER_NAME, value).apply()
            }
        }

    fun getUsers() = users

    companion object {
        lateinit var instance: UserRepository

        fun initialise(application: Application) {
            instance = UserRepository(application)
        }

        private const val USER_PREFERENCES = "nav.enro.example.core.data.USER_PREFERENCES"
        private const val KEY_USER_NAME = "nav.enro.example.core.data.USER_PREFERENCES"
    }
}