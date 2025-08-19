rootProject.name = "Binom-Docker-Api"
include(":docker-api")
include(":testcontainers")

pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        google()
    }
}