import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    `maven-publish`
}

group = "com.github.IndusVeena"
version = "1.0.0"

android {
    namespace = "com.indus.veena.contract"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    compileOnly("com.squareup.okhttp3:okhttp:5.3.2")
    implementation(libs.kotlinx.serialization.json)
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                artifactId = "veena-extension-contract"
            }
        }
    }
}