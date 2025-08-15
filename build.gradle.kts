import pw.binom.publish.getExternalVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.binom.publish) apply false
    alias(libs.plugins.publish.central) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}/*
buildscript {

    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${pw.binom.Versions.KOTLIN_VERSION}")
    }
}
*/
allprojects {
    version = System.getenv("GITHUB_REF_NAME") ?: (property("version") as String?)?.takeIf { it != "unspecified" }
            ?: "1.0.0-SNAPSHOT"
    group = "pw.binom.docker"

//    repositories {
//        mavenLocal()
//        mavenCentral()
//        maven(url = "https://repo.binom.pw")
//    }
}
