package nav.enro.example.feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nav.enro.core.NavigationKey
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination

@Parcelize
data class SearchKey(
    val userId: String
) : NavigationKey

@NavigationDestination(SearchKey::class)
class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}