import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktlint)
}

kotlin {
    task("testClasses")

    js(IR) {
        browser {}
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = "model"
//        browser {
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "data_firebase.js"
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

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(projects.domain)
                implementation(projects.domain.model)

                implementation(libs.koin.core)
                implementation(libs.koin.annotations)

                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.firebase.common.auth)
                implementation(libs.firebase.common.analytics)
                implementation(libs.firebase.common.firestore)
                implementation(libs.firebase.common.functions)
                implementation(libs.firebase.common.messaging)
                implementation(libs.firebase.common.config)

                implementation(libs.kotlinx.datetime)

                implementation(libs.multiplatform.settings)
            }
        }
    }
}

android {
    namespace = "app.wesplit.data.firebase"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

// TODO: Extract this common part to smth on top level to embed shared logic inside all core submodules
// https://github.com/InsertKoinIO/hello-kmp/blob/annotations/shared/build.gradle.kts
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
}

// WORKAROUND: ADD this dependsOn("kspCommonMainKotlinMetadata") instead of above dependencies
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    println("Add dependency from :${this.name}")
    dependsOn("kspCommonMainKotlinMetadata")
}

afterEvaluate {
    tasks.filter {
        it.name.contains("SourcesJar", true)
    }.forEach {
        println("SourceJarTask====>${it.name}")
        it.dependsOn("kspCommonMainKotlinMetadata")
    }
}
