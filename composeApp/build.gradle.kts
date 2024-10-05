import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

kotlin {
    task("testClasses")

    js(IR) {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = "composeApp"
//        browser {
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
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

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.firebase.common.crashlytics)

            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.core.google.shortcuts)

//            implementation(libs.ktor.client.android)
        }

        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                api(projects.domain.model)
                api(projects.domain)
                implementation(projects.data.firebase)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.window.multiplatform)
                implementation(libs.navigation.compose)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.annotations)

//                implementation(libs.bundles.ktor.common)

                api(libs.image.loader)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.io)

                implementation(libs.firebase.common.auth)
                implementation(libs.firebase.common.perf)
                implementation(libs.firebase.common.installations)
                implementation(libs.firebase.common.analytics)
                implementation(libs.firebase.common.firestore)
                implementation(libs.firebase.common.functions)

                implementation(libs.bundles.cupertino)
                implementation(libs.materialKolor)

                implementation(libs.deeplink)
                implementation(libs.kotlinx.datetime)
            }

            desktopMain.dependencies {
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }

            // TODO: Custom set of dependencies 'mobile' could be share if needed
            iosMain.dependencies {
//                implementation(libs.ktor.client.darwin)
                implementation(libs.firebase.common.crashlytics)
            }

//            wasmJsMain.dependencies {
//                implementation(libs.kotlinx.coroutines.wasmjs)
//                implementation(libs.ktor.client.js)
//            }

            jsMain.dependencies {
                implementation(libs.kotlinx.coroutines.js)
//                implementation(libs.ktor.client.js)
                implementation(libs.koin.js)
            }

            commonTest.dependencies {
                dependsOn(commonMain.get())
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
            }
        }
    }

    ksp {
        arg("USE_COMPOSE_VIEWMODEL", "true")
    }
}

android {
    namespace = "app.wesplit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "app.wesplit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.android.versionCode.get().toInt()
        versionName = "${libs.versions.versionName.get()}+${libs.versions.android.versionCode.get()}"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("android-keystore")
            keyAlias = System.getenv("WS_ALIAS")
            storePassword = System.getenv("WS_KEYSTORE_PWD")
            keyPassword = System.getenv("WS_KEY_PWD")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "app.wesplit"
            packageVersion = libs.versions.versionName.get()
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    // DO NOT add bellow dependencies
    add("kspAndroid", libs.koin.ksp.compiler)
    // TODO: IOS fails, maybe 'cause of kotlin 2.0.0
//    add("kspIosX64", Deps.Koin.kspCompiler)
//    add("kspIosArm64", Deps.Koin.kspCompiler)
//    add("kspIosSimulatorArm64", Deps.Koin.kspCompiler)
}

// WORKAROUND: ADD this dependsOn("kspCommonMainKotlinMetadata") instead of above dependencies
// tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
// }
//
// tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
//    println("Add dependency from :${this.name}")
//    dependsOn("kspCommonMainKotlinMetadata")
// }
//
// afterEvaluate {
//    tasks.filter {
//        it.name.contains("SourcesJar", true)
//    }.forEach {
//        println("SourceJarTask====>${it.name}")
//        it.dependsOn("kspCommonMainKotlinMetadata")
//    }
// }

// val javadocJar by tasks.creating(Jar::class) {
//    group = "documentation"
//    archiveClassifier.set("javadoc")
//    from(tasks.dokkaHtml)
// }

subprojects {
    tasks {
        withType<DokkaTask>().configureEach {
            dokkaSourceSets.configureEach {
                includes.from("moduledoc.md")
            }
        }
        withType<DokkaTaskPartial>().configureEach {
            dokkaSourceSets.configureEach {
                includes.from("moduledoc.md")
            }
        }
    }
}
