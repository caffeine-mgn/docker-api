import kotlinx.coroutines.withTimeout
import java.util.*
import java.time.Duration
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
    id("maven-publish")
}

//apply {
//    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
//}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            binaries {
                staticLib()
            }
        }
    }

    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64 {
            binaries {
                staticLib()
            }
        }
    }

//    linuxMips32 {
//        binaries {
//            staticLib()
//        }
//    }
//
//    linuxMipsel32 {
//        binaries {
//            staticLib()
//        }
//    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 { // Use your target instead.
            binaries {
                staticLib()
            }
        }
    }

    macosX64 {
        binaries {
            framework()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("pw.binom.io:httpClient:${pw.binom.Versions.BINOM_VERSION}")
                api("pw.binom.io:concurrency:${pw.binom.Versions.BINOM_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
            }
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
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
        email = "caffeine.mgn@gmail.com"
    )
}
