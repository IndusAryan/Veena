package com.indus.veena.contract

/**
 * Marks the single entry class of a DEX addon.
 *
 * The fully-qualified class name must match [ExtensionManifest.entryPoint]
 * in the developer-authored `src/main/assets/manifest.json`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AddonEntryPoint
