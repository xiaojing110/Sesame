import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.rikka.tools.refine)
}

var isCIBuild: Boolean = System.getenv("CI").toBoolean()
// isCIBuild = true // 没有c++源码时开启CI构建, push前关闭

android {
    namespace = "fansirsqi.xposed.sesame"
    compileSdk = 36

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        splits {
            abi {
                isEnable = true
                reset()
                include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
                isUniversalApk = true
            }
        }
    }

    // 使用 providers API 来支持配置缓存
    val gitCommitCount: Int = providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toIntOrNull() ?: 1

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        applicationId = "fansirsqi.xposed.sesame"
        minSdk = 26
        targetSdk = 36

        val buildDate = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply {
            timeZone = TimeZone.getTimeZone("GMT+8")
        }.format(Date())

        val buildTime = SimpleDateFormat("HH:mm:ss", Locale.CHINA).apply {
            timeZone = TimeZone.getTimeZone("GMT+8")
        }.format(Date())

        versionCode = gitCommitCount
        versionName = "0.9.9"

        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")

        if (isCIBuild) {
            ndk {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            }
        }

        testOptions {
            unitTests.all {
                it.enabled = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
        aidl = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = false // 关闭脱糖
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    signingConfigs {
        getByName("debug") {
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            versionNameSuffix = "-debug"
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    val cmakeFile = file("src/main/cpp/CMakeLists.txt")
    if (!isCIBuild && cmakeFile.exists()) {
        externalNativeBuild {
            cmake {
                path = cmakeFile
                ndkVersion = "29.0.14206865"
            }
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this
            val abiName = output.filters.find { it.filterType == "ABI" }?.identifier ?: "universal"
            val fileName = "Sesame-TK-${abiName}-${variant.versionName}.apk"
            (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = fileName
        }
    }
}

dependencies {
    // Shizuku 相关依赖
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)
    implementation(libs.rikka.refine)

    implementation(libs.cmd.android)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.material3)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    testImplementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.nanohttpd)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.activity.compose)

    implementation(libs.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.slf4j.api)
    implementation(libs.logback.android)
    implementation(libs.appcompat)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.material)
    implementation(libs.webkit)

    // LibXposed 101.0.0
    compileOnly(files("libs/api-82.jar"))  // 可选，测试后可删
    compileOnly("io.github.libxposed:api:101.0.0")
    implementation("io.github.libxposed:interface:101.0.0")
    implementation("io.github.libxposed:service:101.0.0")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.okhttp)
    implementation(libs.dexkit)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)

    implementation(libs.hiddenapibypass)
}
