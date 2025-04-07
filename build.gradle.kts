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

    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        val main by compilations.getting
        val glfw by main.cinterops.creating {
            definitionFile.set(project.file("src/nativeInterop/cinterop/glfw/glfw.def"))
            includeDirs(
                "src/nativeInterop/cinterop/glfw/include",
//                "/usr/include"
            )
        }
        val gl by main.cinterops.creating {
            definitionFile.set(project.file("src/nativeInterop/cinterop/gl/gl.def"))
            includeDirs(
                "src/nativeInterop/cinterop/gl/include",
//                "/usr/include"
            )
            //TODO link lib C:\Program Files (x86)\Windows Kits\10\Lib\10.0.26100.0\um\x64
        }

        binaries {
            executable {
                entryPoint = "io.github.etieskrill.test.kmp.main"
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

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
        val wasmJsTest by getting
    }
}
