pluginManagement {
    repositories {
        gradlePluginPortal() // 必须，用于解析Gradle插件
        google() // 谷歌仓库，用于Android插件
        mavenCentral() // 中央仓库
    }
    resolutionStrategy {
        eachPlugin {
            // 强制指定Android Gradle插件版本（避免自动解析失败）
            if (requested.id.id == "com.android.application") {
                useVersion "8.1.1" // 稳定版本，可根据需要调整
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WebViewApp"
include(":app")