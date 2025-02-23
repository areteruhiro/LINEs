import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "crimera"

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    // TODO: Required because build fails without it. Find a way to remove this dependency.
    implementation(libs.guava)
    // Used in JsonGenerator.
    implementation(libs.gson)

    // A dependency to the Android library unfortunately fails the build, which is why this is required.
    compileOnly(project("dummy"))
}

kotlin {
    jvmToolchain(11)
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
