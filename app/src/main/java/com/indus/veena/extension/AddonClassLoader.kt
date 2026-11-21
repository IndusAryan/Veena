package com.indus.veena.extension

import dalvik.system.DexClassLoader

/** D8 names backport helpers from the dependency that happened to own them during compilation.
 * Independently built APKs can generate the same name with different methods. Loading the host's
 * helper for an addon then throws NoSuchMethodError. Keep generated helpers in their own APK,
 * while retaining parent-first identity for the shared contract, Kotlin, and coroutine classes.
 */
internal class AddonClassLoader(archive: String, optimizedDirectory: String, parent: ClassLoader) :
    DexClassLoader(archive, optimizedDirectory, null, parent) {
    @Synchronized
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (name.contains("\$\$ExternalSynthetic")) {
            val local = findLoadedClass(name) ?: try { findClass(name) } catch (_: ClassNotFoundException) { null }
            if (local != null) {
                if (resolve) resolveClass(local)
                return local
            }
        }
        return super.loadClass(name, resolve)
    }
}
