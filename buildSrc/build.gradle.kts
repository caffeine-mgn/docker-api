buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

plugins{
        kotlin("jvm") version "1.6.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.10")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.6.0")
}