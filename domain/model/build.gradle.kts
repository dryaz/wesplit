import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ktlint)
}

kotlin {
    task("testClasses")

    js(IR) {
        browser {}
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = "model"
//        browser {
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "model.js"
//                devServer =
//                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                        static =
//                            (static ?: mutableListOf()).apply {
//                                // Serve sources to debug inside browser
//                                add(projectDirPath)
//                            }
//                    }
//            }
//        }
//        binaries.executable()
//    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "model"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // TODO: Simplification to have FirebaseUser as is for now
            implementation(libs.firebase.common.firestore)
            implementation(libs.firebase.common.auth)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.firebase.common.firestore)
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "app.wesplit.domain.model"
    compileSdk = 34
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
