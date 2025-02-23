plugins {
    id("app.revanced.patches")
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("app.revanced:revanced-patcher:1.0.0-dev.5")
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("org.smali:smali:2.5.2")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")
    implementation("com.android.tools.build:gradle:8.2.0")
}

patches {
    about {
        name = "ReVanced Patches LIMEs"
        description = "LINE Modification Enhancements for ReVanced"
        source = "https://github.com/areteruhiro/LINEs"
        author = "LIME Team"
        contact = "limebeta.dev@gmail.com"
        website = "https://limes.example.com"
        license = "GNU General Public License v3.0"
    }
}