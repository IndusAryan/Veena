import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version("2.3.21")
    `maven-publish`
}

group = "com.github.IndusVeena"
version = "1.0.0"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

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