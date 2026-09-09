@file:Suppress("UnstableApiUsage")

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor


plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.bugsnag.gradle)
}

val appVersionCode = rootProject.extra["appVersionCode"].toString().toInt()
val androidMinSdk = rootProject.extra["androidMinSdk"].toString().toInt()
val androidTargetSdk = rootProject.extra["androidTargetSdk"].toString().toInt()
val androidCompileSdk = rootProject.extra["androidCompileSdk"].toString().toInt()

val appVersionName = rootProject.extra["appVersionName"].toString()
val appPackageName = rootProject.extra["appPackageName"].toString()

val javaVersion = rootProject.extra["javaVersion"] as JavaVersion

android {
    namespace = appPackageName

    compileSdk = androidCompileSdk
    defaultConfig {
        applicationId = appPackageName
        minSdk = androidMinSdk
        targetSdk = androidTargetSdk
        versionCode = appVersionCode
        versionName = appVersionName
    }
    packaging {
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "version"
    productFlavors {
        create("foss")
        create("play")
    }
}

dependencies {
    implementation(project(":common"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// Workaround for ktor 3.2.0.
// https://youtrack.jetbrains.com/issue/KTOR-8583/Space-characters-in-SimpleName-error-when-executing-R8-mergeExtDex-task-with-3.2.0
androidComponents {
    onVariants { variant ->
        variant.instrumentation.transformClassesWith(
            FieldSkippingClassVisitor.Factory::class.java,
            scope = InstrumentationScope.ALL,
        ) { params ->
            params.classes.add("io.ktor.client.plugins.Messages")
        }
    }
}

class FieldSkippingClassVisitor(
    apiVersion: Int,
    nextClassVisitor: ClassVisitor,
) : ClassVisitor(apiVersion, nextClassVisitor) {

    // Returning null from this method will cause the ClassVisitor to strip all fields from the class.
    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? = null

    abstract class Factory : AsmClassVisitorFactory<Parameters> {
        private val excludedClasses
            get() = parameters.get().classes.get()

        override fun isInstrumentable(classData: ClassData): Boolean =
            classData.className in excludedClasses

        override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
            return FieldSkippingClassVisitor(
                apiVersion = instrumentationContext.apiVersion.get(),
                nextClassVisitor = nextClassVisitor,
            )
        }
    }

    abstract class Parameters : InstrumentationParameters {
        @get:Input
        abstract val classes: SetProperty<String>
    }
}

afterEvaluate {
    base {
        archivesName.set("HINT_Control_${android.defaultConfig.versionName}")
    }
}

