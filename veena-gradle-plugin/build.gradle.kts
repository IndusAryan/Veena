plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version("2.3.21")
    `maven-publish`
}

group = "com.github.IndusVeena"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("veenaExtension") {
            id = "com.indus.veena.extension"
            implementationClass = "com.indus.veena.gradle.VeenaExtensionPlugin"
            displayName = "Veena Extension Builder"
            description = "Validates addon manifest and packages release APK as .veena"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            artifactId = "veena-gradle-plugin"
        }
    }
}