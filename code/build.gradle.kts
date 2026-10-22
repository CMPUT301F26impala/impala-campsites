// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.github.node-gradle.node") version "7.1.0"
}

val managedNodeVersion = "20.19.0"

node {
    version.set(managedNodeVersion)
    npmVersion.set("10.8.2")
    download.set(true)
    nodeProjectDir.set(rootDir)
}

val npmInstall = tasks.named("npmInstall")

tasks.register("startFirebaseEmulators") {
    group = "firebase"
    description = "Starts the Firebase emulators for debug builds."
    notCompatibleWithConfigurationCache("Starts a detached long-running Firebase emulator process.")
    dependsOn(npmInstall)

    doLast {
        val sdkDirectory = java.util.Properties().apply {
            rootDir.resolve("local.properties").inputStream().use(::load)
        }.getProperty("sdk.dir")
        val adbExecutable = sdkDirectory?.let {
            rootDir.resolve(it).resolve("platform-tools").resolve(
                if (System.getProperty("os.name").contains("Windows")) "adb.exe" else "adb"
            )
        }

        if (adbExecutable?.isFile == true) {
            val reverseExitCode = ProcessBuilder(
                adbExecutable.absolutePath,
                "reverse",
                "tcp:8080",
                "tcp:8080"
            )
                .inheritIO()
                .start()
                .waitFor()

            if (reverseExitCode != 0) {
                logger.warn("Could not configure adb reverse for the Firestore emulator.")
            }
        }

        val nodeDirectoryName = if (System.getProperty("os.name").contains("Windows")) {
            "node-v$managedNodeVersion-win-x64"
        } else {
            "node-v$managedNodeVersion-linux-x64"
        }
        val nodeBinDirectory = rootDir.resolve(".gradle/nodejs/$nodeDirectoryName")
        val nodeExecutable = if (System.getProperty("os.name").contains("Windows")) {
            nodeBinDirectory.resolve("node.exe")
        } else {
            nodeBinDirectory.resolve("bin/node")
        }
        val firebaseScript = rootDir.resolve("node_modules/firebase-tools/lib/bin/firebase.js")

        try {
            ProcessBuilder(
                nodeExecutable.absolutePath,
                firebaseScript.absolutePath,
                "emulators:start",
                "--only",
                "firestore",
                "--project",
                "campala-b8671"
            )
                .directory(rootDir)
                .inheritIO()
                .start()
        } catch (error: java.io.IOException) {
            throw GradleException(
                "Firebase CLI could not be started from the Gradle-managed Node installation.",
                error
            )
        }
    }
}