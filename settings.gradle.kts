include("dummy")

rootProject.name = "lines"

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            url = uri("https://maven.revanced.app/repository")
        }
    }
}
