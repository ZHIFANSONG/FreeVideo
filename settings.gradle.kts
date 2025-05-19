pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral() // 关键：包含Kotlin插件的仓库
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> useVersion("8.10.0")
                "org.jetbrains.kotlin.android" -> useVersion("2.1.20")
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