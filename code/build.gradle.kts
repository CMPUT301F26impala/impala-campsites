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
    dependsOn(npmInstall)

    // copy script-level values into locals so doLast doesn't capture the build script,
    // which the configuration cache can't serialize
    val rootDir = rootDir
    val managedNodeVersion = managedNodeVersion

    doLast {
        val isWindows = System.getProperty("os.name").contains("Windows")
        val sdkDirectory = java.util.Properties().apply {
            rootDir.resolve("local.properties").inputStream().use(::load)
        }.getProperty("sdk.dir")
        val adbExecutable = sdkDirectory?.let {
            rootDir.resolve(it).resolve("platform-tools").resolve(
                if (isWindows) "adb.exe" else "adb"
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

        val nodeBinDirectory = rootDir.resolve(".gradle/nodejs")
            .listFiles { file -> file.isDirectory && file.name.startsWith("node-v$managedNodeVersion-") }
            ?.singleOrNull()
            ?: throw GradleException(
                "Expected exactly one Node $managedNodeVersion install under .gradle/nodejs; try deleting that folder and rebuilding."
            )
        val nodeExecutable = if (isWindows) {
            nodeBinDirectory.resolve("node.exe")
        } else {
            nodeBinDirectory.resolve("bin/node")
        }
        val firebaseScript = rootDir.resolve("node_modules/firebase-tools/lib/bin/firebase.js")
        // firebase-tools runs `java` from PATH and needs JDK 21+, so use the daemon's JDK
        // (pinned in gradle-daemon-jvm.properties) instead of whatever is on the user's PATH
        val javaHome = System.getProperty("java.home")

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
                .apply {
                    environment()["JAVA_HOME"] = javaHome
                    environment()["PATH"] = "$javaHome${File.separator}bin${File.pathSeparator}${environment()["PATH"].orEmpty()}"
                }
                .start()
        } catch (error: java.io.IOException) {
            throw GradleException(
                "Firebase CLI could not be started from the Gradle-managed Node installation.",
                error
            )
        }
    }
}