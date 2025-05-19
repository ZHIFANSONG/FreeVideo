// app/build.gradle.kts
plugins {
    id("com.android.application") // Android应用插件
    id("org.jetbrains.kotlin.android") // Kotlin插件
}

android {
    namespace = "com.ssongg.video" // 替换为你的包名
    compileSdk = 34 // 建议使用最新SDK（如34）

    defaultConfig {
        applicationId = "com.ssongg.video" // 应用ID
        minSdk = 26 // 最低兼容版本
        targetSdk = 34 // 目标版本
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 配置Java/Kotlin编译选项
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // 核心依赖
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "com.google.android.material:material:1.10.0"

    // WebView依赖
    implementation "androidx.webkit:webkit:1.8.0" // 支持现代WebView功能

    // 测试依赖
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}