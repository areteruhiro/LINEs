import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "1.9.0"  // Kotlinバージョンを安定版に変更
    `maven-publish`
    id("com.android.library") version "8.2.0"  // Androidライブラリプラグイン追加
}

group = "app.revanced.patches.line"

repositories {
    mavenCentral()
    google()
    maven("https://maven.revanced.app/repository")
    maven("https://jitpack.io")
}

dependencies {
    implementation("app.revanced:revanced-patcher:17.0.0") {
        exclude(group = "org.smali", module = "smali")
    }
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("org.smali:smali:2.5.2")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("com.google.code.gson:gson:2.10.1")

    // Androidツールチェーン
    compileOnly("com.android.tools.build:gradle:8.2.0")
}

kotlin {
    jvmToolchain(17)  // Java 17に更新
}

android {
    namespace = "app.revanced.patches.line"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Name" to "LINE ReVanced Patches",
            "Description" to "LINE customization patches for ReVanced",
            "Version" to project.version,
            "Timestamp" to System.currentTimeMillis().toString(),
            "Source" to "https://github.com/areteruhiro/LINEs",
            "Author" to "Your Name",
            "License" to "GPL-3.0"
        )
    }
}

tasks.register<DefaultTask>("generateBundle") {
    dependsOn("assembleRelease")

    doLast {
        val androidSdk = System.getenv("ANDROID_HOME") ?: throw GradleException("ANDROID_HOME not set")
        val d8 = File(androidSdk).resolve("build-tools")
            .listFilesOrdered().last().resolve("d8").absolutePath

        exec {
            commandLine(d8, layout.buildDirectory.file("outputs/aar/patches-release.aar").get().asFile.absolutePath)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("linePatches") {
            groupId = "app.revanced.patches"
            artifactId = "line"
            version = project.version.toString()

            artifact("$buildDir/outputs/aar/patches-release.aar") {
                classifier = "full"
                extension = "aar"
            }

            pom {
                name = "LINE ReVanced Patches"
                description = "Custom patches for LINE app modifications"
                url = "https://github.com/areteruhiro/LINEs"

                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                    }
                }

                developers {
                    developer {
                        id = "your-github-id"
                        name = "Your Name"
                        email = "your.email@example.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/areteruhiro/LINEs.git"
                    developerConnection = "scm:git:ssh://github.com:areteruhiro/LINEs.git"
                    url = "https://github.com/areteruhiro/LINEs"
                }
            }
        }
    }
}
