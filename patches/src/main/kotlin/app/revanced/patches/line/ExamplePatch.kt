package app.revanced.patches.line

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method


import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch(
    name = "LINE Package Spoof",
    description = "LINEのパッケージ情報と署名を偽装するパッチ",
    compatiblePackages = [CompatiblePackage("jp.naver.line.android")]
)
object LinePackageSpoofPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        // パッケージ名偽装処理
        spoofPackageName(context)

        // 署名検証回避処理
        bypassSignatureVerification(context)
    }

    private fun spoofPackageName(context: BytecodeContext) {
        context.findClass("Landroid/content/pm/PackageManager;")?.let { pkgManagerClass ->
            pkgManagerClass.methods.filter { it.name == "getPackageInfo" }.forEach { method ->
                method.addInstructions(
                    0,
                    """
                        invoke-static {p1}, Lapp/revanced/patches/line/packagespoof/PackageSpoofUtils;->spoofPackageName(Landroid/content/pm/PackageManager;Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p1
                    """
                )
            }
        }
    }

    private fun bypassSignatureVerification(context: BytecodeContext) {
        context.classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                method.implementation?.instructions?.forEachIndexed { index, instruction ->
                    if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {
                        val ref = (instruction as ReferenceInstruction).reference
                        if (ref.name == "verifySignature" &&
                            ref.definingClass == "Ljp/naver/line/android/util/SignatureVerifier;") {

                            method.replaceInstruction(
                                index,
                                "const/4 v0, 0x1\nreturn v0"
                            )
                        }
                    }
                }
            }
        }
    }
}
package app.revanced.patches.example

import android.content.pm.PackageManager
import java.security.MessageDigest

object `PackageSpoofUtils.kt` {
    // LINE固有の偽装設定
    private const val SPOOFED_PACKAGE_NAME = "jp.naver.line.android"
    private const val OFFICIAL_SIGNATURE_SHA256 = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2"

    /**
     * パッケージ名を偽装
     */
    @JvmStatic
    fun spoofPackageName(pm: PackageManager, originalName: String): String {
        return when {
            originalName.startsWith("jp.naver.line") -> SPOOFED_PACKAGE_NAME
            else -> originalName
        }.also {
            if (it != originalName) {
                println("[Spoof] Package name changed: $originalName -> $it")
            }
        }
    }
