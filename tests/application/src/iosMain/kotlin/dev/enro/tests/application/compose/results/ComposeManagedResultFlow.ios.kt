package dev.enro.tests.application.compose.results

// TODO allow presented uiViewController destination in iOS?
//@NavigationDestination.PlatformOverride(ComposeManagedResultFlow.PresentedResult::class)
//internal val presentedResultScreenForDesktop = navigationDestination<ComposeManagedResultFlow.PresentedResult>(
//    metadata = {
//        directOverlay()
//    }
//) {
//    val targetAlpha = remember { mutableFloatStateOf(0f) }
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = animateFloatAsState(targetValue = targetAlpha.value).value))
//    )
//    SideEffect { targetAlpha.value = 0.32f }
//    DialogWindow(
//        title = "",
//        resizable = false,
//        onCloseRequest = {
//            targetAlpha.value = 0f
//            navigation.close()
//        },
//    ) {
//        Card(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            Column(
//                Modifier
//                    .background(MaterialTheme.colors.background)
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = "Presented",
//                    style = MaterialTheme.typography.h6
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = { navigation.complete("A") }) {
//                    Text("Continue (A)")
//                }
//
//                Button(onClick = { navigation.complete("B") }) {
//                    Text("Continue (B)")
//                }
//            }
//        }
//    }
//}