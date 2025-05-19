// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // 核心 Gradle 插件
        classpath("com.android.tools.build:gradle:8.1.1") // Android Gradle 插件版本
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0") // Kotlin 插件版本（2.0+）

        // ---------------------- Compose 编译器插件（Kotlin 2.0+ 必需）----------------------
        classpath("androidx.compose.compiler:compiler:1.5.3") // Compose 编译器插件
        // --------------------------------------------------------------------------------
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}