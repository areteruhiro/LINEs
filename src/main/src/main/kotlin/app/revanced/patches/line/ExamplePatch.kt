package app.revanced.patches.line

import app.revanced.patcher.patch.Option
import app.revanced.patches.shared.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.shared.settings.ResourceUtils.updatePackageName
import app.revanced.patches.shared.settings.ResourceUtils.updatePatchStatus
import app.revanced.patches.shared.settings.settingsPatch
import app.revanced.util.valueOrThrow

// LINEのMainActivityフィンガープリントを直接宣言
val mainActivityFingerprint = object {
    val second = "LINEのMainActivityフィンガープリントに関する情報" // 実際のフィンガープリント情報をここに設定
}

// LINE用の拡張パッチを直接宣言
val sharedExtensionPatch = object {
    // ここにLINE用の拡張パッチの実装を追加
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
    spoofedPackageSignature = "LINEのAPK署名に置き換え", // LINEの正規署名を指定
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
        sharedExtensionPatch // LINE用の拡張パッチ
    )
}
