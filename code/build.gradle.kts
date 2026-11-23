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

val killFirebaseEmulators = tasks.register("killFirebaseEmulators") {
    group = "firebase"
    description = "Kills any processes listening on Firebase emulator ports (8080, 9099, 4000)."

    doLast {
        val ports = listOf(8080, 9099, 4000)
        val isWindows = System.getProperty("os.name").contains("Windows")
        val currentPid = ProcessHandle.current().pid()

        for (port in ports) {
            val pidsToKill = mutableSetOf<Long>()
            if (isWindows) {
                try {
                    val process = ProcessBuilder("cmd.exe", "/c", "netstat -ano").start()
                    val output = process.inputStream.bufferedReader().readText()
                    process.waitFor()

                    val regex = Regex("""[:.]$port\s+.*LISTENING\s+(\d+)""", RegexOption.IGNORE_CASE)
                    regex.findAll(output).forEach { match ->
                        match.groupValues[1].toLongOrNull()?.let { pid ->
                            if (pid > 0 && pid != currentPid) {
                                pidsToKill.add(pid)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("Could not check netstat for port $port: ${e.message}")
                }
            } else {
                try {
                    val process = ProcessBuilder("lsof", "-ti", ":$port").start()
                    val output = process.inputStream.bufferedReader().readText()
                    process.waitFor()

                    output.lines().forEach { line ->
                        line.trim().toLongOrNull()?.let { pid ->
                            if (pid > 0 && pid != currentPid) {
                                pidsToKill.add(pid)
                            }
                        }
                    }
                } catch (e: Exception) {
                    try {
                        ProcessBuilder("fuser", "-k", "-9", "$port/tcp").start().waitFor()
                    } catch (_: Exception) {
                        logger.warn("Could not check lsof/fuser for port $port: ${e.message}")
                    }
                }
            }

            for (pid in pidsToKill) {
                logger.lifecycle("Killing process $pid listening on port $port...")
                try {
                    ProcessHandle.of(pid).ifPresent { it.destroyForcibly() }
                } catch (_: Exception) {}

                if (isWindows) {
                    try {
                        ProcessBuilder("taskkill", "/F", "/T", "/PID", pid.toString())
                            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                            .redirectError(ProcessBuilder.Redirect.DISCARD)
                            .start()
                            .waitFor()
                    } catch (_: Exception) {}
                } else {
                    try {
                        ProcessBuilder("kill", "-9", pid.toString())
                            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                            .redirectError(ProcessBuilder.Redirect.DISCARD)
                            .start()
                            .waitFor()
                    } catch (_: Exception) {}
                }
            }
        }
    }
}

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
            for (port in listOf(8080, 9099, 4000)) {
                val reverseExitCode = ProcessBuilder(
                    adbExecutable.absolutePath,
                    "reverse",
                    "tcp:$port",
                    "tcp:$port"
                )
                    .inheritIO()
                    .start()
                    .waitFor()

                if (reverseExitCode != 0) {
                    logger.warn("Could not configure adb reverse for port $port.")
                }
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
                "--project",
                "campala-b8671",
                // firebase.json has the real security rules for deploying, but the tests write
                // data the rules wouldn't allow (eg: lotteries owned by made up users), so the
                // emulator uses a config with no rules, which makes it allow everything
                "--config",
                "firebase.emulator.json"
            )
                .directory(rootDir)
                .inheritIO()
                .apply {
                    val javaHome = java.io.File(System.getProperty("java.home"))
                    environment()["JAVA_HOME"] = javaHome.absolutePath
                    environment()["PATH"] =
                        javaHome.resolve("bin").absolutePath +
                            java.io.File.pathSeparator +
                            environment()["PATH"]
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