import pw.binom.Versions
import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate
import pw.binom.publish.ifNotMac

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
    id("maven-publish")
}

kotlin {
    allTargets {
        -"js"
    }
    applyDefaultHierarchyBinomTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("pw.binom.io:httpClient:${Versions.BINOM_VERSION}")
                api("pw.binom.io:concurrency:${Versions.BINOM_VERSION}")
                api("pw.binom.io:coroutines:${Versions.BINOM_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLINX_SERIALIZATION_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        ifNotMac {
            val jvmTest by getting {
                dependsOn(commonTest)
                dependencies {
                    api(kotlin("test"))
                }
            }
        }
    }
}

tasks {
    withType(Test::class) {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}

apply<pw.binom.publish.plugins.PrepareProject>()

extensions.getByType(pw.binom.publish.plugins.PublicationPomInfoExtension::class).apply {
    useApache2License()
    gitScm("https://github.com/caffeine-mgn/docker-api")
    author(
        id = "subochev",
        name = "Anton Subochev",
        email = "caffeine.mgn@gmail.com",
    )
}
