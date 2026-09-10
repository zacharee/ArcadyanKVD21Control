import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlin.native.cocoapods)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val appVersionCode = rootProject.extra["appVersionCode"].toString().toInt()
val androidMinSdk = rootProject.extra["androidMinSdk"].toString().toInt()
val androidCompileSdk = rootProject.extra["androidCompileSdk"].toString().toInt()
val androidTargetSdk = rootProject.extra["androidTargetSdk"].toString().toInt()

val appVersionName = rootProject.extra["appVersionName"].toString()
val appPackageName = rootProject.extra["appPackageName"].toString()

val javaVersion = rootProject.extra["javaVersion"] as JavaVersion

version = appVersionName

kotlin {
    android {
        namespace = "dev.zwander.common"

        compileSdk = androidCompileSdk
        minSdk = androidMinSdk

//        sourceSets {
//            getByName("main") {
//                manifest.srcFile("src/androidMain/AndroidManifest.xml")
//                java.srcDirs("build/generated/moko/androidMain/src")
//            }
//        }

//        defaultConfig {
//            minSdk = androidMinSdk
//        }
//        compileOptions {
//            sourceCompatibility = javaVersion
//            targetCompatibility = javaVersion
//            isCoreLibraryDesugaringEnabled = true
//        }
        lint {
            abortOnError = false
            targetSdk = androidTargetSdk
        }
//        buildFeatures {
//            buildConfig = true
//        }
    }
    jvm("desktop")

    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    listOf(iosArm64, iosSimulatorArm64).forEach {
        it.compilations.getByName("main") {
            cinterops.create("BugsnagHINT") {
                includeDirs("$projectDir/src/nativeInterop/cinterop/Bugsnag")
                definitionFile.set(file("$projectDir/src/nativeInterop/cinterop/Bugsnag.def"))
            }
        }
        it.binaries {
            framework {
                isStatic = true
                binaryOption("bundleVersion", appVersionCode.toString())
                binaryOption(
                    "bundleShortVersionString",
                    appVersionName,
                )
                binaryOption("bundleId", appPackageName)
                export(libs.nsexceptionKt.core)
            }
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    cocoapods {
        version = appVersionCode.toString()
        summary = "HINTControl"
        homepage = "https://zwander.dev"
        ios.deploymentTarget = "15.0"
        osx.deploymentTarget = "10.13"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "common"
            isStatic = true
            export(libs.moko.resources)
            export(libs.nsexceptionKt.core)

            binaryOption("bundleVersion", appVersionCode.toString())
            binaryOption(
                "bundleShortVersionString",
                appVersionName,
            )
            binaryOption("bundleId", appPackageName)
        }
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(libs.runtime)
                implementation(libs.foundation)
                implementation(libs.material3)
                implementation(libs.ui)

                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                api(libs.moko.mvvm.compose)
                api(libs.moko.mvvm.flow.compose)
                api(libs.ktor.client.core)
                api(libs.ktor.client.auth)
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.client.mock)
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.kotlin.reflect)
                api(libs.korlibs.korio)
                api(libs.multiplatformSettings)
                api(libs.multiplatformSettings.noArg)
                api(libs.kstore)
                api(libs.kstore.file)
                api(libs.kmpfile)
                api(libs.kmpfile.filekit)
                api(libs.kmpplatform)
                api(libs.koalaplot)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.datetime)
                api(libs.semver)
                api(libs.filekit.core)
                api(libs.filekit.dialogs)
                api(libs.composedialog)
                api(libs.zwander.materialyou)
                api(libs.material.icons.core)
                api(libs.sqlite)
                api(libs.sqlite.bundled)
                api(libs.room.runtime)
                api(libs.multiplatform.markdown.renderer)
                api(libs.multiplatform.markdown.renderer.m3)
            }
        }
        val nonAppleMain = create("nonAppleMain") {
            dependsOn(commonMain)
        }

        val androidMain = getByName("androidMain") {
            dependsOn(nonAppleMain)
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
                api(libs.google.material)

                api(libs.ktor.client.okhttp)
                api(libs.kotlinx.coroutines.android)
                api(libs.bugsnag.android)
                api(libs.relinker)
                api(libs.androidx.glance.appwidget)

                api(libs.taskerpluginlibrary)
                api(libs.github.api)
            }
        }
        val skiaMain = create("skiaMain") {
            dependsOn(commonMain)
        }
        val desktopMain = getByName("desktopMain") {
            dependsOn(skiaMain)
            dependsOn(nonAppleMain)
            dependencies {
                api(libs.ui.tooling.preview)
                api(compose.desktop.currentOs)

                api(libs.ktor.client.okhttp)
                api(libs.jna)
                api(libs.slf4j.jdk14)
                api(libs.bugsnag.jvm)
                api(libs.jSystemThemeDetector)
                api(libs.oshi.core)
                api(libs.appdirs)
                api(libs.kotlinx.coroutines.swing)
                api(libs.conveyor.control)
            }
        }

        val darwinMain = create("darwinMain") {
            dependsOn(skiaMain)
            dependencies {
                api(libs.ktor.client.darwin)
                api(libs.nsexceptionKt.bugsnag)
                api(libs.nsexceptionKt.core)
                api(libs.nserrorKt)
            }
        }

        val iosArm64Main = getByName("iosArm64Main") {
            resources.srcDirs("build/generated/moko/iosArm64Main/src")
        }
        val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main") {
            resources.srcDirs("build/generated/moko/iosSimulatorArm64Main/src")
        }
        val iosMain = create("iosMain") {
            dependsOn(darwinMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
}

multiplatformResources {
    resourcesPackage.set("dev.zwander.resources.common")
}

buildkonfig {
    packageName = "dev.zwander.common"
    objectName = "GradleConfig"
    exposeObjectWithName = objectName

    defaultConfigs {
        buildConfigField(STRING, "versionName", appVersionName)
        buildConfigField(STRING, "versionCode", "$appVersionCode")
        buildConfigField(STRING, "packageName", appPackageName)
        buildConfigField(STRING, "appName", "${rootProject.extra["appName"]}")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    configurations.filter { it.name.startsWith("ksp") && it.name != "ksp" }.forEach {
        add(it.name, libs.room.compiler)
    }
}

room {
    schemaDirectory("$projectDir/schema")
}

afterEvaluate {
    val setVersionName = providers.exec {
        isIgnoreExitValue = true

        commandLine(
            "/usr/bin/plutil",
            "-replace",
            "CFBundleShortVersionString",
            "-string",
            appVersionName,
            "${rootProject.layout.projectDirectory.asFile.absolutePath}/iosApp/iosApp/Info.plist",
        )
    }

    val setVersionCode = providers.exec {
        isIgnoreExitValue = true

        commandLine(
            "/usr/bin/plutil",
            "-replace",
            "CFBundleVersion",
            "-string",
            "$appVersionCode",
            "${rootProject.layout.projectDirectory.asFile.absolutePath}/iosApp/iosApp/Info.plist",
        )
    }

    try {
        setVersionName.result.get()
        setVersionCode.result.get()

        setVersionName.standardError.asText.get().takeIf { it.isNotBlank() }?.let {
            println(it)
        }
        setVersionCode.standardError.asText.get().takeIf { it.isNotBlank() }?.let {
            println(it)
        }
    } catch (_: Throwable) {}
}
