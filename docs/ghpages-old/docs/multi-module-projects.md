# Multi-module projects
Enro was designed to support large multi-module projects just as well as it supports small projects. There are a few things to keep in mind when using Enro in a multi-module project, but it is generally very simple to set up. 

The most important part of supporting multi-module projects is the fact that NavigationKeys can be defined seperately to the destinations/screens that they are bound to. This means that you can define a NavigationKey in one module, and bind it to a destination in another module.

Exactly how this is done depends on the module structure of the project in question. Here are some examples of different ways that this might work in a multi-module project:
1. A single module contains all the NavigationKeys. Modules that depend on this module can then bind these NavigationKeys to destinations, or use these NavigationKeys for navigation.
2. Each "feature" module defines an "api" or "public" module, which contains all of the NavigationKeys for that feature. Other modules can then depend on this "api" module use these NavigationKeys for navigation. In this situation, it would be expected that there is an "internal", "private" or "implementation" which provides the destinations for these NavigationKeys.

Essentially, to make Enro work across a multi-module project, all you need to do is make sure that the NavigationKeys are defined in a location that is visible to the modules that need to use them, either for binding the destinations or for performing navigation to those NavigationKeys.

For more information on defining NavigationKeys, please see the [NavigationKeys documentation](./navigation-keys.md).