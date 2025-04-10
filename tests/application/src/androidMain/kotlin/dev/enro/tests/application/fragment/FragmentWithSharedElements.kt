package dev.enro.tests.application.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.destination.compose.OverrideNavigationAnimations
import dev.enro.destination.fragment.FragmentSharedElements
import dev.enro.tests.application.R
import dev.enro.tests.application.activity.applyInsetsForContentView
import kotlinx.parcelize.Parcelize

/**
 * FragmentSharedElementDestination is a destination that provides an example of using FragmentSharedElements to create shared
 * element transitions between Fragments and Composables. This example uses a RecyclerView to display a list of images, and
 * allows the user to open a Fragment or Composable that displays the image in a larger format, with a shared element transition
 * going from the RecyclerView to the Fragment/Composable (and back again).
 *
 * Note: The NavigationController must be configured with [FragmentSharedElements.composeCompatibilityPlugin] to allow shared
 * element transitions between Fragments and Composables (it's not required for Fragment-to-Fragment shared element transitions).
 * See [dev.enro.tests.application.TestApplication] where [FragmentSharedElements.composeCompatibilityPlugin] is installed.
 */
@Parcelize
object FragmentSharedElementDestination : Parcelable, NavigationKey.SupportsPresent {
    @Parcelize
    internal object RecyclerViewFragment : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal class DetailViewFragment(
        val imageId: Int
    ) : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal class DetailViewComposable(
        val imageId: Int
    ) : Parcelable, NavigationKey.SupportsPush
}

/**
 * This Activity is not interesting, it's just a container to hold the Fragments/Composables for this example.
 */
@NavigationDestination(FragmentSharedElementDestination::class)
class FragmentSharedElementActivity : AppCompatActivity() {
    val container by navigationContainer(
        containerId = R.id.fragment_container,
        root = { FragmentSharedElementDestination.RecyclerViewFragment },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_shared_element_activity)
        applyInsetsForContentView()
    }
}

/**
 * This imageTints list is used as a way to tint the images in the RecyclerView for this example, the position in the list
 * is considered to be an "id" for the examples below. In reality, you would likely load the data from a remote source.
 */
private val imageTints = listOf(
    0xFFFF0000,
    0xFF00FF00,
    0xFF0000FF,
    0xFFFF00FF,
    0xFFFFFF00,
    0xFF00FFFF,
)

/**
 * The FragmentSharedElementRecyclerView is a simple RecyclerView that displays a list of images, each one tinted to a different
 * color based on [imageTints]. Each row in the RecyclerView can open a Fragment or Composable that displays the image in a
 * larger "detail" format. There is a shared element transition between FragmentSharedElementRecyclerView and
 * FragmentSharedElementDetailFragment/FragmentSharedElementDetailComposable.
 */
@NavigationDestination(FragmentSharedElementDestination.RecyclerViewFragment::class)
class FragmentSharedElementRecyclerView : Fragment() {

    private val navigation by navigationHandle<NavigationKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutManager = LinearLayoutManager(requireContext())
            adapter = object : RecyclerView.Adapter<SharedElementViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedElementViewHolder {
                    val view = LinearLayout(requireContext())
                    view.layoutParams =
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    view.setVerticalGravity(Gravity.CENTER_VERTICAL)
                    view.orientation = LinearLayout.HORIZONTAL
                    view.addView(ImageView(requireContext()).apply {
                        setImageResource(R.drawable.ic_launcher_foreground)
                    })
                    view.addView(Button(requireContext()).apply {
                        text = "Open Fragment"
                    })
                    view.addView(Button(requireContext()).apply {
                        text = "Open Compose"
                    })
                    return SharedElementViewHolder(view)
                }

                override fun onBindViewHolder(holder: SharedElementViewHolder, position: Int) {
                    holder.bind(position)
                }

                override fun getItemCount(): Int = imageTints.size
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // It's required to slightly delay enter transitions when using RecyclerViews, according to the following documentation:
        // https://developer.android.com/guide/fragments/animate#recyclerview
        // The following code will delay the enter transition until the RecyclerView is ready to draw:
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    inner class SharedElementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(imageId: Int) {
            val view = itemView as LinearLayout
            view.getChildAt(0).apply {
                this as ImageView
                transitionName = "image_$imageId"
                imageTintList = ColorStateList.valueOf(imageTints[imageId].toInt())
            }
            view.getChildAt(1).setOnClickListener {
                FragmentSharedElements.addSharedElement(view.getChildAt(0), "sharedElementImage")
                navigation.push(FragmentSharedElementDestination.DetailViewFragment(imageId))
            }
            view.getChildAt(2).setOnClickListener {
                FragmentSharedElements.addSharedElement(view.getChildAt(0), "sharedElementImage")
                navigation.push(FragmentSharedElementDestination.DetailViewComposable(imageId))
            }
        }
    }
}

@NavigationDestination(FragmentSharedElementDestination.DetailViewFragment::class)
class FragmentSharedElementDetailFragment : Fragment() {

    private val navigation by navigationHandle<FragmentSharedElementDestination.DetailViewFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireNotNull(container)

        val imageSize = maxOf(container.measuredWidth, container.measuredHeight) / 2
        return LinearLayout(requireContext()).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
            orientation = LinearLayout.VERTICAL
            val image = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(imageSize, imageSize)
                transitionName = "sharedElementImage"
                setImageResource(R.drawable.ic_launcher_foreground)
                imageTintList = ColorStateList.valueOf(imageTints[navigation.key.imageId].toInt())
            }
            addView(image)
            addView(Button(requireContext()).apply {
                text = "Close"
                FragmentSharedElements.addSharedElement(image, "image_${navigation.key.imageId}")
                setOnClickListener {
                    navigation.requestClose()
                }
            })
        }
    }
}

@OptIn(AdvancedEnroApi::class)
@NavigationDestination(FragmentSharedElementDestination.DetailViewComposable::class)
@Composable
fun FragmentSharedElementDetailComposable() {
    // It's important to call FragmentSharedElements.ConfigureComposable() when you want to configure an
    // @Composable @NavigationDestination to support shared elements that are shared with Fragments.
    // In this case, we're also calling OverrideNavigationAnimations() to override the default navigation animations, so
    // that the other elements on the screen fade in and out, which puts a focus on the shared element animation.
    FragmentSharedElements.ConfigureComposable()

    // We're also going to configure a delayed transition, so that the shared element transition can be delayed until we're
    // ready to draw the View associated with the shared element. This is important for shared elements that are not
    // immediately visible when the view is drawn. It's not strictly necessary in this case, but it's a useful example.
    val delayedTransition = FragmentSharedElements.rememberDelayedTransitionController()

    val navigation = navigationHandle<FragmentSharedElementDestination.DetailViewComposable>()

    OverrideNavigationAnimations(fadeIn(), fadeOut()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val constraints = constraints
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val imageSize = maxOf(constraints.maxWidth, constraints.maxHeight) / 2
                val imageSizeDp = LocalDensity.current.run { imageSize.toDp() }

                AndroidView(
                    modifier = Modifier.size(imageSizeDp),
                    factory = { context ->
                        ImageView(context).apply {
                            setImageResource(R.drawable.ic_launcher_foreground)
                            imageTintList =
                                ColorStateList.valueOf(imageTints[navigation.key.imageId].toInt())
                            transitionName = "sharedElementImage"
                            FragmentSharedElements.addSharedElement(
                                this,
                                "image_${navigation.key.imageId}"
                            )

                            // Wait until the view is ready to draw before starting the transition
                            doOnPreDraw { delayedTransition.start() }
                        }
                    },
                )

                Button(
                    onClick = {
                        navigation.requestClose()
                    }
                ) {
                    Text("Close")
                }
            }
        }
    }
}


