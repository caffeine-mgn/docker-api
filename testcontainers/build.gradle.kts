import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.binom.publish)
    alias(libs.plugins.publish.central)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    withSourcesJar(publish = true)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            api(kotlin("stdlib-common"))
            api(project(":docker-api"))
            api(libs.binom.io.http.client.core)
            api(libs.binom.io.concurrency)
            api(libs.binom.io.coroutines)
            api(libs.kotlinx.serialization.json)
            api(kotlin("test-common"))
            api(kotlin("test-annotations-common"))
            api(libs.kotlinx.coroutines.test)
        }
//        jvmMain.dependencies {
//            api(kotlin("test-junit"))
//        }

//        val commonTest by getting {
//            dependencies {
//            }
//        }
//        val jvmTest by getting {
//            dependsOn(commonTest)
//            dependencies {
//                api(kotlin("test"))
//            }
//        }
    }
}

tasks {
    withType(Test::class) {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}


fun Project.propertyOrNull(property: String) =
    if (hasProperty(property)) property(property) as String else null

val keyId = propertyOrNull("binom.gpg.key_id")
val password = propertyOrNull("binom.gpg.password")
val privateKey = propertyOrNull("binom.gpg.private_key")

if (keyId != null && password != null && privateKey != null) {
    signing {
        useInMemoryPgpKeys(keyId, privateKey.replace("|", "\n"), password)
    }
}

val binomUser = propertyOrNull("binom.repo.user")
val binomPassword = propertyOrNull("binom.repo.password")

if (binomUser != null && binomPassword != null) {
    publishing {
        repositories {
            maven {
                name = "Binom"
                url = uri("https://repo.binom.pw")
                credentials {
                    username = binomUser
                    password = binomPassword
                }
            }
        }
    }
}

val repoSlug = "caffeine-mgn/docker-api"

publishOnCentral {
    if (project.description != null) {
        projectDescription.set(project.description)
    }
//    projectLongName.set(extra["projectLongName"].toString())
    projectUrl.set("https://github.com/$repoSlug")
    licenseName.set("Apache-2.0 license")
    licenseUrl.set(projectUrl.map { "$it/blob/main/LICENSE" })
    scmConnection.set("git:git@github.com:$repoSlug.git")
    repoOwner.set("caffeine-mgn")
    projectUrl.set("https://github.com/$repoSlug")
}

val token = System.getenv("GITHUB_TOKEN")
if (token != null) {
    publishOnCentral {
        repository("https://maven.pkg.github.com/${repoSlug.lowercase()}") {
            user.set("caffeine-mgn")
            password.set(System.getenv("GITHUB_TOKEN"))
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        developers {
            developer {
                name.set("Subochev Anton")
                email.set("caffeine.mgn@gmail.com")
                url.set("https://github.com/caffeine-mgn")
                roles.set(mutableSetOf("architect", "developer"))
            }
        }
    }
}

tasks {
    val singTasks = withType<Sign>()
    withType<AbstractPublishToMaven>().all {
        dependsOn(singTasks)
    }
    val releaseMavenCentralPortalPublication by getting
    val zipMavenCentralPortalPublication by getting

    releaseMavenCentralPortalPublication.dependsOn(zipMavenCentralPortalPublication)
}
//apply<pw.binom.publish.plugins.PrepareProject>()
