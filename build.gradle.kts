import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "1.9.22"  // Kotlin安定版を使用
    `maven-publish`
}

group = "app.revanced.patches.line"

repositories {
    mavenCentral()
    google()
    maven(url = "https://maven.revanced.app/repository")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(libs.revanced.patcher)
    compileOnly(project("dummy"))
}

kotlin {
    jvmToolchain(17)  // Java 17に更新
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Name" to "LINE ReVanced Patches",
            "Description" to "LINE customization patches for ReVanced",
            "Version" to project.version,
            "Source" to "https://github.com/areteruhiro/LINEs",
            "Author" to "Your Name",
            "License" to "GPL-3.0"
        )
    }
}

tasks.register<DefaultTask>("generateBundle") {
    dependsOn("jar")

    doLast {
        val androidHome = System.getenv("ANDROID_HOME") ?: throw GradleException("ANDROID_HOME environment variable not set")
        val buildTools = File(androidHome, "build-tools").listFilesOrdered()?.last()
            ?: throw GradleException("No Android build-tools found")

        val d8 = File(buildTools, "d8").takeIf { it.exists() }?.absolutePath
            ?: throw GradleException("d8 tool not found")

        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val outputDir = layout.buildDirectory.dir("libs").get().asFile

        exec {
            workingDir = outputDir
            commandLine(d8, "--release", "--output", outputDir.absolutePath, jarFile.absolutePath)
        }

        exec {
            workingDir = outputDir
            commandLine("zip", "-uj", jarFile.absolutePath, "classes.dex")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("linePatches") {
            from(components["java"])
            artifactId = "line-patches"
            version = project.version.toString()

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
                    developerConnection = "scm:git:ssh://github.com/areteruhiro/LINEs.git"
                    url = "https://github.com/areteruhiro/LINEs"
                }
            }
        }
    }
}
