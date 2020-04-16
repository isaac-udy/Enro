package nav.enro.example.feature

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DetailKey(
    val userId: String,
    val id: String
) : NavigationKey

class DetailActivity : AppCompatActivity() {
    private val navigation by navigationHandle<DetailKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(TextView(this).apply {
            text = "Detail View ${navigation.key.id}"
        })
    }
}