package app.revanced.patches.line

import app.revanced.patcher.patch.Option
import app.revanced.patches.shared.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.shared.settings.ResourceUtils.updatePackageName
import app.revanced.patches.shared.settings.ResourceUtils.updatePatchStatus
import app.revanced.patches.shared.settings.settingsPatch
import app.revanced.util.valueOrThrow

// LINEのMainActivityフィンガープリントを直接宣言
internal val mainActivityFingerprint = legacyFingerprint(
    name = "lineMainActivityFingerprint",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf(
        "android.intent.action.MAIN",
        "FEline_home"
    ),
    customFingerprint = { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("Activity;")
    }
)

// LINE用の拡張パッチを直接宣言
val sharedExtensionPatch = object {
    // LINE用のapplicationInitHook
    val applicationInitHook = app.revanced.patches.line.utils.extension.hooks.applicationInitHook

    // LINE用のcronetEngineContextHook
    val cronetEngineContextHook = app.revanced.patches.shared.extension.hooks.cronetEngineContextHook

    // LINE用のfirebaseInitProviderContextHook
    val firebaseInitProviderContextHook = app.revanced.patches.shared.extension.hooks.firebaseInitProviderContextHook

    // LINE用のmainActivityBaseContextHook
    val mainActivityBaseContextHook = app.revanced.patches.line.utils.extension.hooks.mainActivityBaseContextHook
}

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = "jp.naver.line.android", // LINEのパッケージ名
    mainActivityOnCreateFingerprint = mainActivityFingerprint.second, // LINEのMainActivityフィンガープリント
    extensionPatch = sharedExtensionPatch, // LINE用の拡張パッチ
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(COMPATIBLE_PACKAGE) // 互換性のあるパッケージを指定
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
    packageNameLineOption: Option<String>
) = app.revanced.patches.shared.gms.gmsCoreSupportResourcePatch(
    fromPackageName = "jp.naver.line.android", // LINEのパッケージ名
    spoofedPackageSignature = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2", // LINEの正規署名を指定
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    packageNameYouTubeOption = packageNameLineOption, // LINEのパッケージ名を使用
    packageNameYouTubeMusicOption = packageNameLineOption, // LINEのパッケージ名を使用
    executeBlock = {
        updatePackageName(
            gmsCoreVendorGroupIdOption.valueOrThrow() + ".android.gms", // GMS CoreのベンダーグループIDを更新
            packageNameLineOption.valueOrThrow() // LINEのパッケージ名を更新
        )

        updatePatchStatus("GMSCORE_SUPPORT") // GMSCORE_SUPPORTのステータスを更新
    },
) {
    dependsOn(
        settingsPatch, // 必要な依存関係を追加
        sharedExtensionPatch // LINE用の拡張パッチを依存関係に追加
    )
}
