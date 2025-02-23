plugins {
    id("java")
      id("app.revanced.library.patches")    
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
dependencies {
    implementation("app.revanced:revanced-patcher:17.0.0")
}
