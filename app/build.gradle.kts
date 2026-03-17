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
                // version = "4.1.2" // 不要随意改这个了答应我
                ndkVersion = "29.0.14206865" // 这个也是 答应我就这样吧
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

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }  // LibXposed 目前主要通过 jitpack 分发
}

dependencies {
    // Shizuku 相关依赖 - 用于获取系统级权限
    implementation(libs.rikka.shizuku.api)      // Shizuku API
    implementation(libs.rikka.shizuku.provider) // Shizuku 提供者
    implementation(libs.rikka.refine)           // Rikka 反射工具
    // implementation(libs.rikka.hidden.stub)

    implementation(libs.cmd.android)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.material3)              // Material 3（可能与 Compose 一起用）

    // Compose 相关依赖 - 现代化 UI 框架
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00") // Compose BOM 版本管理
    implementation(composeBom)
    testImplementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.material3)           // Material 3 设计组件
    implementation(libs.androidx.ui.tooling.preview)  // UI 工具预览
    debugImplementation(libs.androidx.ui.tooling)     // 调试时的 UI 工具
    implementation(libs.androidx.material.icons.extended)

    // 生命周期和数据绑定
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // JSON 序列化
    implementation(libs.kotlinx.serialization.json)

    // Kotlin 协程依赖 - 异步编程
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // 数据观察和 HTTP 服务
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.nanohttpd)  // 轻量级 HTTP 服务器

    // UI 布局和组件
    implementation(libs.androidx.constraintlayout)
    implementation(libs.activity.compose)

    // Android 核心库
    implementation(libs.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.slf4j.api)
    implementation(libs.logback.android)
    implementation(libs.appcompat)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.material)
    implementation(libs.webkit)

    // ─── LibXposed 101.0.0 相关依赖（模块开发者标准组合） ───
    // 传统 Xposed API 82（可选保留，用于兼容极旧框架，测试后可删除）
    compileOnly(files("libs/api-82.jar"))

    // LibXposed API 定义（仅编译时需要）
    compileOnly("io.github.libxposed:api:101.0.0")

    // 模块接口实现（hook 入口等，必须 implementation）
    implementation("io.github.libxposed:interface:101.0.0")

    // 服务通信库（新版标准，必须 implementation）
    implementation("io.github.libxposed:service:101.0.0")

    // 如果你是框架/服务端开发者才需要 implementation api（普通模块不要加）
    // implementation("io.github.libxposed:api:101.0.0")

    // 代码生成和工具库
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.okhttp)
    implementation(libs.dexkit)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)

    // 核心库脱糖和系统 API 访问（你已注释，如需要可打开）
    // coreLibraryDesugaring(libs.desugar)
    implementation(libs.hiddenapibypass)
}
