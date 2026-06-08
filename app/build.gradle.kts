import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "2.3.21"
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.indus.veena"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.indus.veena"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

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
            val releaseSigning = signingConfigs.findByName("release")
            signingConfig = if (releaseSigning?.storeFile?.exists() == true) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            resValue("string", "app_name", "Veena Debug")
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    //noinspection WrongGradleMethod
    kotlin {
       compilerOptions {
           jvmTarget.set(JvmTarget.JVM_17)
       }
        //(17)
    }
    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }

    signingConfigs {
        create("release") {
            val isCI = System.getenv("CI") != null // GitHub Actions sets this automatically
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (isCI) {
                storeFile = file(keystorePath ?: error("KEYSTORE_PATH env var is not set"))
                storePassword = System.getenv("STORE_PASSWORD") ?: error("STORE_PASSWORD env var is not set")
                keyAlias = System.getenv("ALIAS") ?: error("ALIAS env var is not set")
                keyPassword = System.getenv("PASSWORD") ?: error("PASSWORD env var is not set")
            }
        }
    }
}

dependencies {
    implementation(project(":extension-contract"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    implementation(libs.androidx.palette.ktx)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.transformer)
    implementation(libs.androidx.media3.exoplayer.hls)

    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.compose.material3.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.taglib)

    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.quickjs.kt)
    implementation(libs.quickjs.kt.converter.ktxserialization)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    /*implementation(libs.ytm.kt) {
        // Exclude the old extractor version
        exclude(group = "com.github.teamnewpipe.newpipeextractor", module = "extractor")
        // Exclude the new extractor version
        exclude(group = "com.github.teamnewpipe.NewPipeExtractor", module = "extractor")
        // Exclude the old timeago-parser version
        exclude(group = "com.github.teamnewpipe.newpipeextractor", module = "timeago-parser")
        // Exclude the new timeago-parser version
        exclude(group = "com.github.teamnewpipe.NewPipeExtractor", module = "timeago-parser")
    }*/
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.json)
}