package com.indus.veena.gradle

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

abstract class BuildVeenaPluginTask : DefaultTask() {

    @get:InputFile
    abstract val manifestFile: RegularFileProperty

    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputDirectory
    abstract val releaseApkDir: DirectoryProperty

    @TaskAction
    fun build() {
        val manifest = readAndValidateManifest(manifestFile.get().asFile)
        val annotatedEntryPoints = findAnnotatedEntryPoints(sourceDir.get().asFile)

        when (annotatedEntryPoints.size) {
            0 -> throw GradleException(
                "No @AddonEntryPoint class found. Annotate exactly one MusicAddon implementor."
            )
            1 -> Unit
            else -> throw GradleException(
                "Multiple @AddonEntryPoint classes found: ${annotatedEntryPoints.joinToString()}. " +
                    "Only one entry point is allowed per addon."
            )
        }

        val declaredEntryPoint = manifest.requireString("entryPoint")
        val detectedEntryPoint = annotatedEntryPoints.single()
        if (declaredEntryPoint != detectedEntryPoint) {
            throw GradleException(
                "manifest.json entryPoint \"$declaredEntryPoint\" does not match " +
                    "@AddonEntryPoint class \"$detectedEntryPoint\"."
            )
        }

        val extensionId = manifest.requireString("id")
        val apkFile = releaseApkDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == "apk" }
            .maxByOrNull { it.lastModified() }
            ?: throw GradleException("No release APK found under ${releaseApkDir.get().asFile}")

        val outDir = outputDir.get().asFile.also { it.mkdirs() }
        val veenaFile = File(outDir, "$extensionId.veena")
        apkFile.copyTo(veenaFile, overwrite = true)

        verifyPackagedManifest(veenaFile, manifest)

        val sha256 = sha256Of(veenaFile)
        File(outDir, "$extensionId.veena.sha256").writeText(sha256)

        logger.lifecycle("------------------------------------------------------------")
        logger.lifecycle("Addon ID     : $extensionId")
        logger.lifecycle(
            "Version      : ${manifest.requireString("version")} (code ${manifest.requireInt("versionCode")})"
        )
        logger.lifecycle("Entry point  : $detectedEntryPoint")
        logger.lifecycle("Output       : ${veenaFile.absolutePath}")
        logger.lifecycle("SHA-256      : $sha256")
        logger.lifecycle("------------------------------------------------------------")
    }

    private fun readAndValidateManifest(file: File): JsonObject {
        if (!file.exists()) {
            throw GradleException(
                "Missing ${file.path}. Create src/main/assets/manifest.json with all required fields."
            )
        }

        val manifestJson = try {
            json.parseToJsonElement(file.readText()).jsonObject
        } catch (e: Exception) {
            throw GradleException("manifest.json is not valid JSON: ${e.message}")
        }

        listOf(
            "id", "name", "version", "versionCode", "apiVersion",
            "capabilities", "entryPoint"
        ).forEach { field ->
            if (manifestJson[field] == null) {
                throw GradleException("manifest.json is missing required field: \"$field\"")
            }
        }

        val extensionId = manifestJson.requireString("id")
        if (!extensionId.matches(Regex("[a-z0-9_]+"))) {
            throw GradleException("id must be lowercase alphanumeric/underscore, got: \"$extensionId\"")
        }

        if (manifestJson.requireInt("versionCode") < 1) {
            throw GradleException("versionCode must be >= 1")
        }

        if (manifestJson.requireString("entryPoint").isBlank()) {
            throw GradleException("entryPoint must be the fully-qualified @AddonEntryPoint class name")
        }

        return manifestJson
    }

    private fun findAnnotatedEntryPoints(srcDir: File): List<String> {
        val results = mutableListOf<String>()
        val classPattern = Regex(
            """@AddonEntryPoint\s*(?:\([^)]*\))?\s*(?:(?:public|open|abstract|data|internal|final)\s+)*class\s+(\w+)""",
            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )

        srcDir.walkTopDown()
            .filter { it.isFile && it.extension in listOf("kt", "java") }
            .forEach { file ->
                val content = file.readText()
                if (!content.contains("@AddonEntryPoint")) return@forEach

                val packageMatch = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
                    .find(content)
                    ?: throw GradleException("No package declaration in ${file.name}")

                val classMatch = classPattern.find(content)
                    ?: throw GradleException("@AddonEntryPoint found but no class declaration in ${file.name}")

                results.add("${packageMatch.groupValues[1]}.${classMatch.groupValues[1]}")
            }

        return results
    }

    private fun verifyPackagedManifest(veenaFile: File, expectedManifest: JsonObject) {
        ZipFile(veenaFile).use { zip ->
            val entry = zip.getEntry("assets/manifest.json")
                ?: throw GradleException(
                    "assets/manifest.json missing from APK. " +
                        "Place manifest.json in src/main/assets/ before building."
                )

            val packaged = json.parseToJsonElement(
                zip.getInputStream(entry).bufferedReader().readText()
            ).jsonObject

            if (packaged.requireString("entryPoint") != expectedManifest.requireString("entryPoint")) {
                throw GradleException(
                    "Packaged manifest.json entryPoint does not match source manifest.json"
                )
            }
        }
    }

    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun JsonObject.requireString(key: String): String {
        val element = this[key] ?: throw GradleException("manifest.json is missing required field: \"$key\"")
        return element.jsonPrimitive.content
    }

    private fun JsonObject.requireInt(key: String): Int {
        val element = this[key] ?: throw GradleException("manifest.json is missing required field: \"$key\"")
        return element.jsonPrimitive.int
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
