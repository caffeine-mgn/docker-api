buildscript {

    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("com.bmuschko:gradle-docker-plugin:6.6.1")
    }
}

allprojects {
    version = pw.binom.Versions.LIB_VERSION
    group = "pw.binom.io"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}