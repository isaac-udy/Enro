package dev.enro.example.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize
import dev.enro.annotations.NavigationDestination
import dev.enro.example.core.navigation.SearchKey

@NavigationDestination(SearchKey::class)
class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}