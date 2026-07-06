// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(shared.plugins.android.application) apply false
    alias(shared.plugins.kotlin.android) apply false
    alias(shared.plugins.kotlin.compose) apply false
    alias(shared.plugins.android.library) apply false
}