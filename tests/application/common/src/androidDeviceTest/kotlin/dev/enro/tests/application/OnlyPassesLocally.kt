package dev.enro.tests.application

/**
 * Marks a test that passes locally and fails on CI for an unknown reason.
 */
annotation class OnlyPassesLocally(
    val description: String
)