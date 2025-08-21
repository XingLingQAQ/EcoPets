import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.0"
}

group = "com.willfp"
version = findProperty("version")!!
// 修复：添加非空断言或默认值
val libreforgeVersion = findProperty("libreforge-version") ?: "未知版本"

base {
    archivesName.set(project.name)
}

// 修复：移除不正确的依赖声明方式
// 正确的依赖声明应该在 allprojects 或 subprojects 块中

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://mvn.lumine.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("com.willfp:eco:6.56.0")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
        
        // 如果你需要添加对 eco-core 子项目的依赖
        // 可以在这里添加，但需要确保这些项目存在
        if (project.name != "eco-core") {
            // 添加对 eco-core 的依赖
            implementation(project(":eco-core"))
        }
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks {
        shadowJar {
            relocate("com.willfp.libreforge.loader", "com.willfp.ecopets.libreforge.loader")
            relocate("com.willfp.ecomponent", "com.willfp.ecopets.ecomponent")
            relocate("com.willfp.modelenginebridge", "com.willfp.ecopets.modelenginebridge")
        }

        compileKotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    // 修复：确保使用非空值
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

// 如果你确实需要添加 eco-core 的所有子项目作为依赖
// 可以在 afterEvaluate 中处理
afterEvaluate {
    // 获取 eco-core 的所有子项目并添加为依赖
    val ecoCoreProject = project(":eco-core")
    if (ecoCoreProject.subprojects.isNotEmpty()) {
        ecoCoreProject.subprojects.forEach { subproject ->
            // 为当前项目添加对 eco-core 子项目的依赖
            dependencies {
                implementation(subproject)
            }
        }
    }
}
