package nav.enro.example.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nav.enro.core.NavigationKey
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.example.core.navigation.SearchKey

@NavigationDestination(SearchKey::class)
class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}