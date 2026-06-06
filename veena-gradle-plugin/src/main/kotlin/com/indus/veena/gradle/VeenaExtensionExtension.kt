package com.indus.veena.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
abstract class VeenaExtensionExtension {
    abstract val manifestFile: RegularFileProperty
    abstract val outputDir: DirectoryProperty
    abstract val sourceDir: DirectoryProperty
}
