import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "1.9.22"
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
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.smali:smali:2.5.2")
}

kotlin {
    jvmToolchain(17)
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

tasks {
    register<DefaultTask>("generateBundle") {
        dependsOn("jar")

        doLast {
            val androidHome = System.getenv("ANDROID_HOME") 
                ?: throw GradleException("ANDROID_HOME environment variable not set")
            
            val buildToolsDir = File(androidHome, "build-tools")
            val buildTools = buildToolsDir.listFilesOrdered()
                ?.lastOrNull()
                ?: throw GradleException("No Android build-tools found in $buildToolsDir")

            val d8 = File(buildTools, "d8").takeIf { it.exists() }
                ?: throw GradleException("d8 tool not found in $buildTools")

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

    register<JavaExec>("generatePatchesFiles") {
        group = "revanced"
        description = "Generate patches metadata files"
        
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.generator.MainKt")
        
        dependsOn("build")
        
        inputs.files(sourceSets["main"].allSource.srcDirs)
        outputs.dir(layout.buildDirectory.dir("generated/patches"))
        
        doFirst {
            mkdir(layout.buildDirectory.dir("generated/patches"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("revanced-patches-publication") {
            artifactId = "line-patches"
            from(components["java"])
            
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
