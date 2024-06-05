plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.aryan.veena"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aryan.veena"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        val jdk = JavaVersion.VERSION_17
        sourceCompatibility = jdk
        targetCompatibility = jdk
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Networking
    implementation(libs.retrofit2)
    implementation(libs.squareup.logging.interceptor)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.retrofit)

    // Android core
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    
    // Design
    implementation(libs.material)
    implementation(libs.shimmer)
    implementation(libs.androidx.constraintlayout)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media)

    // Image
    implementation(libs.coil)
    implementation(libs.quickie.bundled) // qr scan

    // Navigation
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}