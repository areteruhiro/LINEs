package app.revanced.patches.line

import app.revanced.patcher.patch.Option
import app.revanced.patches.shared.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.shared.settings.ResourceUtils.updatePackageName
import app.revanced.patches.shared.settings.ResourceUtils.updatePatchStatus
import app.revanced.patches.shared.settings.settingsPatch
import app.revanced.util.valueOrThrow

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = "jp.naver.line.android", // LINEのパッケージ名
    mainActivityOnCreateFingerprint = mainActivityFingerprint.second,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(COMPATIBLE_PACKAGE)
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
    packageNameLineOption: Option<String>
) = app.revanced.patches.shared.gms.gmsCoreSupportResourcePatch(
    fromPackageName = "jp.naver.line.android",
    spoofedPackageSignature = "LINEのAPK署名に置き換え", // LINEの正規署名を指定
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    packageNameYouTubeOption = packageNameLineOption,
    packageNameYouTubeMusicOption = packageNameLineOption,
    executeBlock = {
        updatePackageName(
            gmsCoreVendorGroupIdOption.valueOrThrow() + ".android.gms",
            packageNameLineOption.valueOrThrow()
        )

        updatePatchStatus("GMSCORE_SUPPORT") // GMSCORE_SUPPORTのステータスを更新
    },
) {
    dependsOn(
        // 必要な依存関係を追加
        settingsPatch,
        sharedExtensionPatch
    )
}
