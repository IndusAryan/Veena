pluginManagement {
    includeBuild("veena-gradle-plugin")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.toastbits.dev")
        maven("https://maven.syk.sh/snapshots")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
        maven("https://maven.toastbits.dev")
        maven("https://maven.syk.sh/snapshots")
    }
}

rootProject.name = "Veena"
include(":app")
include(":extension-contract")
include(":YTMusicPlugin")
include(":NewPipePlugin")
include(":SoundCloudPlugin")
include(":saavn-addon")
