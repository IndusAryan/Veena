// Top-level build file where you can add configuration options common to all sub-projects/modules.
allprojects {
    repositories {
        maven { url = uri("https://www.jitpack.io" ) }
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}