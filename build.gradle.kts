// build.gradle.kts（项目根目录）
buildscript {
    // 不需要手动声明仓库，已在settings.gradle.kts中配置
}

allprojects {
    // 清空，不添加任何仓库配置
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}