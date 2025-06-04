package dev.enro.tests.application.managedflow

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object ManagedFlowInFragment : Parcelable, NavigationKey.SupportsPresent {
    @Parcelize
    internal class ResultFragment(
        val userInformation: UserInformation,
    ) : Parcelable, NavigationKey.SupportsPush
}

@NavigationDestination(ManagedFlowInFragment::class)
class ManagedFlowInFragmentActivity : AppCompatActivity() {
    init {
        TODO("ACTIVITY STUFF")
    }
//    private val navigation by navigationHandle<ManagedFlowInFragment>()
//    private val container by navigationContainer(
//        containerId = R.id.fragment_container,
//        root = { null },
//        emptyBehavior = EmptyBehavior.CloseParent,
//        filter = accept {
//            key<UserInformationFlow>()
//            key<ManagedFlowInFragment.ResultFragment>()
//        },
//        interceptor = {
//            onResult<UserInformationFlow, UserInformation> { _, _ ->
//                deliverResultAndCancelClose()
//            }
//        }
//    )
//
//    private val getUserInformation by registerForNavigationResult<UserInformation> {
//        navigation.push(ManagedFlowInFragment.ResultFragment(it))
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.managed_flow_in_fragment_activity)
//        applyInsetsForContentView()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (container.backstack.isEmpty()) {
//            getUserInformation.push(UserInformationFlow())
//        }
//    }
}

@NavigationDestination(ManagedFlowInFragment.ResultFragment::class)
class UserInformationResultFragment : Fragment() {

    private val navigation by navigationHandle<ManagedFlowInFragment.ResultFragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                TitledColumn(
                    title = "User Information",
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Name: ${navigation.key.userInformation.name}")
                    Text("Email: ${navigation.key.userInformation.email}")
                    Text("Age: ${navigation.key.userInformation.age}")
                }
            }
        }
    }
}