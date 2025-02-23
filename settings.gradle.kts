pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            url = uri("https://maven.revanced.app/repository")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.revanced.app/repository")
        }
    }
}

rootProject.name = "LINEs"

include(":app")
include(":patches")