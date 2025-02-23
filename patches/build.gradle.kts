plugins {
    id("app.revanced.library.patches") version "17.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
}

dependencies {
    implementation("app.revanced:revanced-patcher:17.0.0")
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("org.smali:smali:2.5.2")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")
    implementation("com.android.tools.build:gradle:8.2.0")
}

patches {
    about {
        name = "LINE ReVanced Patches"
        description = "LINE Modification Enhancements for ReVanced"
        source = "https://github.com/areteruhiro/LINEs"
        author = "LIME Team"
        contact = "limebeta.dev@gmail.com"
        website = "https://limes.example.com"
        license = "GPL-3.0"
    }
}