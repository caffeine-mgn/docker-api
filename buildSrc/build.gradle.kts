buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.binom.pw")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    }
}

val kotlinVersion = project.property("kotlin.version") as String
val binomVersion = project.property("binom.version") as String

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
    buildConfigField("String", "BINOM_VERSION", "\"$binomVersion\"")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.binom.pw")
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.6.0")
    api("pw.binom:binom-publish:0.1.2")
}
