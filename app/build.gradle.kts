plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.22"
}

/*val tmpFilePath = System.getProperty("user.home") + "/work/_temp/keystore/"
val releaseStoreFile: File? = File(tmpFilePath).listFiles()?.first()*/

android {
    namespace = "com.aryan.veena"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aryan.veena"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

        signingConfigs {
            create("release") {
                //storeFile = releaseStoreFile?.let { file(it) }
                storeFile = file(System.getenv("SIGNING_KEY_STORE_PATH") ?: "/home/runner/keystore.jks")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }

        buildTypes {
            release {
                signingConfig = signingConfigs["release"]
                isMinifyEnabled = false
                isShrinkResources = false
                isDebuggable = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

    splits {
        abi {
            isEnable = true
            reset()
            //noinspection ChromeOsAbiSupport
            include("arm64-v8a")
            isUniversalApk = true
        }
    }

    compileOptions {
        val jdk = JavaVersion.VERSION_17
        sourceCompatibility = jdk
        targetCompatibility = jdk
        isCoreLibraryDesugaringEnabled = true
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
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Design
    implementation(libs.material)
    implementation(libs.shimmer)
    implementation(libs.androidx.constraintlayout)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    //implementation(libs.androidx.media3.exoplayer.dash)
    //implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media)
    implementation(libs.nicehttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ytmkt.android)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    // newpipe for yt
    implementation(libs.converter.scalars)
    implementation(libs.newpipeextractor)
    // Image
    implementation(libs.coil)

    // Navigation
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}