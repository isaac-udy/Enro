# dev.enro.core.serialization

This package contains helper functions for wrapping and unwrapping primitive types and collection types. This is primarily used for serializing and deserializing data to and from the NavigationInstructionExtras objects that are included in NavigationInstruction.Open classes.

The primary way of interacting with this package is through `Any?.wrapForSerialization()` and `Any.unwrapForSerialization`. These functions will use WrappedCollection and WrappedPrimitive subclasses to wrap values, which is important for correctly serializing and deserializing values included in NavigationInstructionExtras, which is essentially a `Map<String, Any>`. 