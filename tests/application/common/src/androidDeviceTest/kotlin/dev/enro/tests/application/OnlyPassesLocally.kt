package dev.enro.tests.application

/**
 * This annotation is used to mark a test that only passes locally, and fails on CI for some reason,
 * there are a few tests that just don't seem to pass on CI, and this is a way to mark them as such
 * so that they can be easily identified.
 */
annotation class OnlyPassesLocally(
    val description: String
)