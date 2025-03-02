package app.revanced.patches.line.misc.gmscore

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.line.utils.extension.hooks.applicationInitHook
import app.revanced.patches.shared.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.compatibility.Constants.COMPATIBLE_PACKAGE

@Patch(
    name = "LINE GMS Core Support",
    description = "Google Mobile Services (GMS) サポートを有効化",
    dependencies = [
        gmsCoreSupportPatch::class, // 依存パッチ
        settingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "jp.naver.line.android",
            ["12.0.0", "13.5.0"] // 対応バージョン範囲
        )
    ]
)
class LineGmsCoreSupportPatch : BytecodePatch(
    fingerprints = listOf(mainActivityFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // 1. メインアクティビティのフック設定
        val mainActivityClass = context.findClass(mainActivityFingerprint)
            ?: throw PatchException("MainActivity not found")

        // 2. GMS Core 初期化処理の挿入
        mainActivityClass.methods.first { it.name == "onCreate" }.apply {
            addInstructions(
                0,
                """
                    invoke-static {p0}, ${applicationInitHook.declaringClass}->initialize(Landroid/content/Context;)V
                """
            )
        }

        // 3. リソースの更新
        updatePackageName("custom.gms.provider")
        updatePatchStatus("GMS_ENABLED")
    }

    companion object {
        // フィンガープリントの共有化
        val mainActivityFingerprint = methodFingerprint(
            returnType = "V",
            parameters = listOf("Landroid/os/Bundle;"),
            strings = listOf("FEline_home", "MainActivity")
        )
    }
}
