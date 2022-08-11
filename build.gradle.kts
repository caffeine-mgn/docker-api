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

allprojects {
    version = System.getenv("GITHUB_REF_NAME") ?: "1.0.0-SNAPSHOT"
    group = "pw.binom.io"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.binom.pw")
    }
}
