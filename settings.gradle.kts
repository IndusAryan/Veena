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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
    }
}

rootProject.name = "Veena"
include(":app")
include(":extension-contract")
include(":YTMusicPlugin")
include(":NewPipePlugin")
include(":SoundCloudPlugin")
include(":saavn-addon")
