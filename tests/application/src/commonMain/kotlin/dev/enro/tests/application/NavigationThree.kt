package dev.enro.tests.application

//
//@Composable
//@NavigationDestination(NavigationThree::class)
//fun Example() {
//    val backstack = rememberNavBackStack(ListKey)
//
//    NavDisplay(
//        backStack = backstack,
//        entryProvider = entryProvider {
//            entry<ListKey> {
//                Column {
//                    repeat(50) {
//                        Text(
//                            modifier = Modifier
//                                .padding(8.dp)
//                                .fillMaxWidth()
//                                .clickable {
//                                    backstack.add(ExampleDetail(it.toString()))
//                                },
//                            text = it.toString(),
//                        )
//                    }
//                }
//            }
//
//            entry<ExampleDetail> {
//                TitledColumn("Details") {
//                    Text("ID: ${it.id}")
//                }
//            }
//        }
//    )
//}