// 修正後の build.gradle
rootProject.name = "revanced-patches-line"  // プロジェクト名をLINE特化に変更

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        // GitHub Packagesリポジトリは不要なため削除
    }
}

plugins {
    id("app.revanced.patches") version "2.0.0"  // 最新安定版に更新
    id("org.jetbrains.kotlin.android") version "1.9.0"  // Kotlinプラグイン追加
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

dependencies {
    // コア依存関係
    implementation("com.android.tools.build:gradle:8.2.0")  // AGPバージョン更新
    implementation("org.jf.dexlib2:dexlib2:2.5.2")
    implementation("app.revanced:revanced-patcher:19.0.0")  // 最新パッチャー

    // LINE固有の依存関係
    implementation("com.google.code.gson:gson:2.10.1")  // 設定ファイル処理用
    implementation("org.smali:smali:2.5.2")  // バイトコード操作

    // テスト用
    testImplementation("junit:junit:4.13.2")
}

// パッチ固有の設定
revancedPatches {
    targetPackage = "jp.naver.line.android"
    signatureSpoofing {
        enabled = true
        keyStorePath = file("line-keystore.jks")
    }
    resourceOverlays {
        enable = true
    }
}

// カスタムタスクの追加
tasks.register("generateLinePatches") {
    group = "revanced"
    dependsOn(":app:assembleDebug")
    doLast {
        println("LINE specific patches generated")
    }
}