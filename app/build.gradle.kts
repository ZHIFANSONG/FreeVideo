plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.compose.ui.tooling.preview") version "1.5.3" apply false // 可选：Compose 预览插件
    kotlin("kapt") // 若使用注解处理器（如 Hilt），需添加
}

android {
    namespace = "com.example.myapp" // 替换为你的应用包名
    compileSdk = 34 // 建议设置为最新版本（如 34+）

    defaultConfig {
        applicationId = "com.example.myapp" // 应用 ID
        minSdk = 26 // 最低支持版本（根据需求调整）
        targetSdk = 34 // 目标版本（建议与 compileSdk 一致）
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            minifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // ---------------------- Compose 配置 ----------------------
    composeOptions {
        // 配置 Compose 编译器版本（必须与 buildscript 中的 compiler 版本一致）
        compilerOptions {
            kotlinCompilerExtensionVersion = "1.5.3" // 与 buildscript 中的版本匹配
        }
    }

    // 启用 AndroidX 兼容性
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // --------------------------------------------------------------------------------
}

dependencies {
    // ---------------------- Compose 核心库 ----------------------
    implementation platform("androidx.compose.ui:ui-bom:1.5.3")
    implementation platform("androidx.compose.ui:ui-graphics-bom:1.5.3")
    implementation platform("androidx.compose.ui:ui-tooling-preview-bom:1.5.3")

    // 基础库（必须）
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-graphics-android")
    implementation("androidx.compose.ui:ui-tooling-preview") {
        exclude(module = "ui-tooling")
    }

    // Android 平台适配（必须）
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    releaseImplementation("androidx.compose.ui:ui-tooling-preview:1.5.3") {
        exclude(module = "ui-tooling")
    }
    // --------------------------------------------------------------------------------

    // 其他依赖（如 Material Design 3）
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui.tooling.preview:ui-tooling-preview")
    implementation platform("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.activity:activity-compose")

    // 测试依赖
    debugImplementation("androidx.compose.ui.tooling:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation platform("androidx.compose.ui:ui-tooling-preview-bom:1.5.3")
    androidTestImplementation("androidx.compose.ui.tooling:ui-tooling-preview") {
        exclude(module = "ui-tooling")
    }
}