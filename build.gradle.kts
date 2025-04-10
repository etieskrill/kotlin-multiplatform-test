import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.etieskrill.test.kmp"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.rootDir.path)
                    }
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val nativeMain by creating { dependsOn(commonMain) }
        val nativeTest by creating { dependsOn(commonTest) }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
        val wasmJsTest by getting
    }

    val nativeMain by sourceSets.getting
    val nativeTest by sourceSets.getting

    for (nativeTarget in listOf(
        mingwX64("windows"),
        linuxX64("linux")
    )) {
        val nativeLibs = when (nativeTarget.name) {
            "windows" -> "win"
            "linux" -> "linux"
            else -> throw GradleException("Unknown target: $nativeTarget")
        }

        nativeTarget.apply {
            val main by compilations.getting { defaultSourceSet.dependsOn(nativeMain) }
            main.cinterops.create("glfw-$nativeLibs") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/glfw/glfw-$nativeLibs.def"))
                includeDirs("src/nativeInterop/cinterop/glfw/include")
            }
            main.cinterops.create("gl-$nativeLibs") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/gl/gl-$nativeLibs.def"))
                includeDirs("src/nativeInterop/cinterop/gl/include")
            }

            binaries {
                executable {
                    entryPoint = "io.github.etieskrill.test.kmp.main"
                }
            }
        }

        val hostOs = System.getProperty("os.name")
        val isArm64 = System.getProperty("os.arch") == "aarch64"
        val isMingwX64 = hostOs.startsWith("Windows")

        val hostNativeTargetName = when {
//            hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
//            hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
//            hostOs == "Linux" && isArm64 -> linuxArm64("native")
            hostOs == "Linux" && !isArm64 -> "linux"
            isMingwX64 -> "windows"
            else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        }
        if (nativeTarget.name != hostNativeTargetName) continue

        task("runReleaseExecutableNative") {
            group = "run"
            dependsOn("runReleaseExecutable${hostNativeTargetName.uppercaseFirstChar()}")
        }
    }

    task("run") {
        group = "run"
        dependsOn("runReleaseExecutableNative")
    }

    task("runBrowser") {
        group = "run"
        dependsOn("wasmJsBrowserProductionRun")
    }
}
