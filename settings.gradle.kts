rootProject.name = "revanced-patches-template"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/revanced/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

plugins {
    id("app.revanced.library.patches") version "17.0.0"
}

dependencies {
    implementation("app.revanced:revanced-patcher:17.0.0")
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("org.smali:smali:2.5.2")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")
}