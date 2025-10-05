---
title: Navigation Destinations  
parent: Core Concepts
nav_order: 2
---

# Navigation Destinations

NavigationDestinations are the implementations that fulfill NavigationKey contracts. They're the
actual screens that users see and interact with.

## The Core Concept

A NavigationDestination binds a NavigationKey to a UI implementation:

```kotlin
NavigationKey → NavigationDestination → User sees a screen
```

When you navigate to a NavigationKey, Enro finds the corresponding destination and renders it.

## Composable Destinations

The primary way to create destinations in Enro 3.x is with Composables.

### Basic Composable Destination

```kotlin
@Composable
@NavigationDestination(HomeScreen::class)
fun HomeScreenDestination() {
    val navigation = navigationHandle()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Welcome Home!")
        Button(onClick = { navigation.close() }) {
            Text("Close")
        }
    }
}
```

**Key elements:**

- `@Composable` - Standard Compose annotation
- `@NavigationDestination(HomeScreen::class)` - Binds to NavigationKey
- `navigationHandle()` - Gets the navigation controller

### Accessing Parameters

Use `navigationHandle<T>()` with a type parameter to access the NavigationKey:

```kotlin
@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileDestination() {
    val navigation = navigationHandle<UserProfile>()
    
    // Access parameters from the key
    val userId = navigation.key.userId
    val tab = navigation.key.tab
    
    Column {
        Text("Profile for user: $userId")
        Text("Showing tab: $tab")
    }
}
```

### Destinations with Results

For NavigationKeys that return results:

```kotlin
@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorDestination() {
    val navigation = navigationHandle<SelectColor>()
    
    LazyColumn {
        items(availableColors) { color ->
            ColorOption(
                color = color,
                isSelected = color == navigation.key.currentColor,
                onClick = {
                    // Return the selected color
                    navigation.complete(color)
                }
            )
        }
    }
}
```

**Navigation operations:**

- `navigation.complete(result)` - Complete with a result (required for `WithResult` keys)
- `navigation.complete()` - Complete without a result (positive action/confirmation)
- `navigation.close()` - Close without completing (dismissal/cancellation)
- `navigation.completeFrom(key)` - Delegate result to another screen

Any screen can use both `complete()` and `close()`. The distinction is semantic:

- Use `complete()` for positive actions (save, confirm, select)
- Use `close()` for dismissals or cancellations (back button, cancel, dismiss)

## Property-Based Destinations

For special rendering requirements (dialogs, bottom sheets), use the property-based approach:

```kotlin
@NavigationDestination(ConfirmDialog::class)
val confirmDialog = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) {
    AlertDialog(
        title = { Text(navigation.key.title) },
        text = { Text(navigation.key.message) },
        onDismissRequest = { navigation.close() },
        confirmButton = {
            Button(onClick = { navigation.complete(true) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { navigation.complete(false) }) {
                Text("Cancel")
            }
        }
    )
}
```

**Advantages:**

- Access to `NavigationDestinationScope`
- Clean syntax for dialog/overlay destinations
- Automatic metadata configuration

### Navigation Destination Scope

The `NavigationDestinationScope` provides:

```kotlin
@NavigationDestination(ExampleScreen::class)
val exampleDestination = navigationDestination<ExampleScreen> {
    // 'this' is NavigationDestinationScope
    
    navigation         // NavigationHandle<ExampleScreen>
    destinationMetadata // Metadata for this destination
    
    // Also implements AnimatedVisibilityScope and SharedTransitionScope
    // for animations and shared element transitions
}
```

## Scene Metadata

Use metadata to control how destinations are rendered:

### Dialog Scenes

```kotlin
@NavigationDestination(ErrorDialog::class)
val errorDialog = navigationDestination<ErrorDialog>(
    metadata = { dialog() }
) {
    Dialog(onDismissRequest = { navigation.close() }) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(navigation.key.errorMessage)
                Button(onClick = { navigation.close() }) {
                    Text("OK")
                }
            }
        }
    }
}
```

### Direct Overlay Scenes

For custom overlay behavior without a dialog window:

```kotlin
@NavigationDestination(LoadingOverlay::class)
val loadingOverlay = navigationDestination<LoadingOverlay>(
    metadata = { directOverlay() }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

### Custom Metadata

Add your own metadata for custom behavior:

```kotlin
object FullScreenMetadataKey : NavigationKey.MetadataKey<Boolean>(default = false)

@NavigationDestination(VideoPlayer::class)
val videoPlayer = navigationDestination<VideoPlayer>(
    metadata = {
        add(FullScreenMetadataKey, true)
    }
) {
    // Destination can check metadata and adjust behavior
    // ...
}
```

## Fragment Destinations (Android)

> **Note:** Fragment support requires the `enro-compat` module.

```kotlin
dependencies {
    implementation("dev.enro:enro-compat:3.0.0-alpha05")
}
```

### Basic Fragment

```kotlin
@NavigationDestination(SettingsScreen::class)
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    
    private val navigation by navigationHandle<SettingsScreen>()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Access parameters
        val section = navigation.key.section
        
        // Setup UI
        view.findViewById<Button>(R.id.close_button).setOnClickListener {
            navigation.close()
        }
    }
}
```

### Fragment with Results

```kotlin
@NavigationDestination(SelectItem::class)
class SelectItemFragment : Fragment(R.layout.fragment_select_item) {
    
    private val navigation by navigationHandle<SelectItem>()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView.adapter = ItemAdapter { item ->
            // Return the selected item
            navigation.complete(item)
        }
    }
}
```

### Fragment Containers

Fragments can host their own navigation containers:

```kotlin
@NavigationDestination(TabContainer::class)
class TabContainerFragment : Fragment(R.layout.fragment_tab_container) {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val containerView = view.findViewById<FragmentContainerView>(R.id.container)
        
        // Setup container for child navigation
        childFragmentManager.createFragmentNavigationContainer(
            containerId = containerView.id,
            filter = accept { /* filter logic */ }
        )
    }
}
```

## Activity Destinations (Android)

> **Note:** Activity support requires the `enro-compat` module.

### Basic Activity

```kotlin
@NavigationDestination(FullscreenVideo::class)
class VideoPlayerActivity : ComponentActivity() {
    
    private val navigation by navigationHandle<FullscreenVideo>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val videoUrl = navigation.key.videoUrl
            
            VideoPlayer(
                url = videoUrl,
                onClose = { navigation.close() }
            )
        }
    }
}
```

### Activity with Result

```kotlin
@NavigationDestination(CapturePhoto::class)
class CameraActivity : ComponentActivity() {
    
    private val navigation by navigationHandle<CapturePhoto>()
    
    private fun onPhotoTaken(photo: Bitmap) {
        val uri = savePhoto(photo)
        navigation.complete(uri)
    }
}
```

## Manual Binding (Without Annotation Processor)

If you prefer not to use the annotation processor, bind destinations manually:

```kotlin
@NavigationComponent
object AppNavigationComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        // Composable destination
        destination(
            navigationDestination<UserProfile> {
                UserProfileScreen()
            }
        )
        
        // Property-based destination with metadata
        destination(
            navigationDestination<ConfirmDialog>(
                metadata = { dialog() }
            ) {
                ConfirmDialogContent()
            }
        )
    }
)
```

## Platform-Specific Destinations

For multiplatform projects, provide different implementations per platform:

### Common Key

```kotlin
// commonMain
@Serializable
data class MapView(val location: Location) : NavigationKey
```

### Android Implementation

```kotlin
// androidMain
@Composable
@NavigationDestination(MapView::class)
fun AndroidMapView() {
    val navigation = navigationHandle<MapView>()
    AndroidView(factory = { context ->
        GoogleMapView(context).apply {
            // Configure map
        }
    })
}
```

### iOS Implementation

```kotlin
// iosMain
@Composable
@NavigationDestination(MapView::class)
fun IosMapView() {
    val navigation = navigationHandle<MapView>()
    UIKitView(factory = {
        // Create MKMapView
    })
}
```

### Platform Override

Use `@NavigationDestination.PlatformOverride` when you need both a common and platform-specific
implementation:

```kotlin
// commonMain - fallback implementation
@NavigationDestination(MapView::class)
@Composable
fun CommonMapView() {
    Text("Map view not available on this platform")
}

// androidMain - platform-specific override
@NavigationDestination.PlatformOverride(MapView::class)
@Composable
fun AndroidMapView() {
    // Android-specific map implementation
}
```

## Accessing NavigationHandle

### In Composables

```kotlin
@Composable
fun MyScreen() {
    // Untyped - when you don't need the key
    val navigation = navigationHandle()
    
    // Typed - when you need access to parameters
    val navigation = navigationHandle<UserProfile>()
}
```

### In ViewModels

```kotlin
class ProfileViewModel : ViewModel() {
    private val navigation by navigationHandle<UserProfile>()
    
    val userId = navigation.key.userId
    
    fun close() {
        navigation.close()
    }
}
```

### In Fragments

```kotlin
class ProfileFragment : Fragment() {
    private val navigation by navigationHandle<UserProfile>()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userId = navigation.key.userId
    }
}
```

### In Activities

```kotlin
class ProfileActivity : ComponentActivity() {
    private val navigation by navigationHandle<UserProfile>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = navigation.key.userId
    }
}
```

## Lifecycle and State

### Composable Lifecycle

Composable destinations follow standard Compose lifecycle:

```kotlin
@Composable
@NavigationDestination(ExampleScreen::class)
fun ExampleDestination() {
    val navigation = navigationHandle()
    
    DisposableEffect(Unit) {
        // On enter
        println("Screen entered")
        
        onDispose {
            // On exit
            println("Screen exited")
        }
    }
    
    LaunchedEffect(Unit) {
        // Suspend operations
        loadData()
    }
}
```

### State Preservation

Enro automatically preserves state through:

- Configuration changes
- Process death
- Navigation operations

```kotlin
@Composable
@NavigationDestination(FormScreen::class)
fun FormDestination() {
    // State is automatically saved/restored
    var text by rememberSaveable { mutableStateOf("") }
    var selection by rememberSaveable { mutableStateOf(0) }
    
    TextField(
        value = text,
        onValueChange = { text = it }
    )
}
```

### Custom State Saving

For complex state:

```kotlin
@Composable
@NavigationDestination(ComplexScreen::class)
fun ComplexDestination() {
    val navigation = navigationHandle()
    
    var complexState by rememberSaveable(
        stateSaver = ComplexState.Saver
    ) {
        mutableStateOf(ComplexState())
    }
}

@Serializable
data class ComplexState(
    val items: List<Item> = emptyList(),
    val selection: Set<String> = emptySet()
) {
    companion object {
        val Saver = enroSaver<ComplexState>()
    }
}
```

## Best Practices

### Keep Destinations Thin

Destinations should focus on UI. Move business logic to ViewModels:

```kotlin
// ✅ GOOD
@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileDestination(
    viewModel: ProfileViewModel = viewModel {
        createEnroViewModel { ProfileViewModel() }
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileContent(uiState)
}

// ❌ BAD - too much logic in destination
@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileDestination() {
    var user by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(Unit) {
        user = repository.loadUser() // Don't do this
    }
    // ...
}
```

### Use Descriptive Names

```kotlin
// ✅ GOOD - Clear function names
@NavigationDestination(UserProfile::class)
fun UserProfileScreen() { }

@NavigationDestination(SelectDate::class)
fun DatePickerDialog() { }

// ❌ BAD - Generic names
@NavigationDestination(UserProfile::class)
fun Destination1() { }
```

### One Destination Per Key

```kotlin
// ✅ GOOD - One destination per key
@NavigationDestination(UserProfile::class)
fun UserProfileScreen() { }

// ❌ BAD - Multiple destinations for same key
@NavigationDestination(UserProfile::class)
fun UserProfileScreen1() { }

@NavigationDestination(UserProfile::class)  // Compilation error!
fun UserProfileScreen2() { }
```

### Organize by Feature

```kotlin
// features/profile/ProfileDestinations.kt
@NavigationDestination(UserProfile::class)
@Composable
fun UserProfileScreen() { }

@NavigationDestination(EditProfile::class)
@Composable
fun EditProfileScreen() { }

// features/settings/SettingsDestinations.kt
@NavigationDestination(Settings::class)
@Composable
fun SettingsScreen() { }
```

## Common Patterns

### Master-Detail

```kotlin
@Composable
@NavigationDestination(MasterDetail::class)
fun MasterDetailScreen() {
    val navigation = navigationHandle<MasterDetail>()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    
    if (isTablet) {
        Row {
            MasterPane(modifier = Modifier.weight(0.3f))
            DetailPane(modifier = Modifier.weight(0.7f))
        }
    } else {
        MasterPane(modifier = Modifier.fillMaxSize())
    }
}
```

### Wizard Steps

```kotlin
@Composable
@NavigationDestination(WizardStep1::class)
fun WizardStep1Screen() {
    val navigation = navigationHandle()
    
    WizardStepContent(
        title = "Step 1 of 3",
        onNext = {
            navigation.open(WizardStep2())
        }
    )
}
```

### Conditional Content

```kotlin
@Composable
@NavigationDestination(ContentScreen::class)
fun ContentDestination() {
    val navigation = navigationHandle<ContentScreen>()
    
    when (navigation.key.contentType) {
        ContentType.TEXT -> TextContent()
        ContentType.IMAGE -> ImageContent()
        ContentType.VIDEO -> VideoContent()
    }
}
```

## Next Steps

- [Navigation Operations](navigation-operations.md) - Learn how to navigate between destinations
- [Result Handling](result-handling.md) - Master returning and receiving results
- [Navigation Containers](navigation-containers.md) - Build complex navigation hierarchies

---

**Questions?** Check the [FAQ](../faq.md) or open an issue
on [GitHub](https://github.com/isaac-udy/Enro).
