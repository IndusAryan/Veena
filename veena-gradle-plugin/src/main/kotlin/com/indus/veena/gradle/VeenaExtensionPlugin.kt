package com.indus.veena.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class VeenaExtensionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("veenaExtension", VeenaExtensionExtension::class.java)
        extension.manifestFile.convention(project.layout.projectDirectory.file("src/main/assets/manifest.json"))
        extension.sourceDir.convention(project.layout.projectDirectory.dir("src/main"))
        extension.outputDir.convention(project.layout.buildDirectory.dir("veena"))

        val buildTask: TaskProvider<BuildVeenaPluginTask> =
            project.tasks.register("buildVeenaPlugin", BuildVeenaPluginTask::class.java)

        buildTask.configure {
            setGroup("veena")
            setDescription("Validates developer manifest and packages release APK as .veena")
            dependsOn("assembleRelease")

            manifestFile.set(extension.manifestFile)
            sourceDir.set(extension.sourceDir)
            outputDir.set(extension.outputDir)
            releaseApkDir.set(project.layout.buildDirectory.dir("outputs/apk/release"))
        }

        project.tasks.register("veena").configure {
            setGroup("veena")
            setDescription("Alias for buildVeenaPlugin")
            dependsOn(buildTask)
        }
    }
}
