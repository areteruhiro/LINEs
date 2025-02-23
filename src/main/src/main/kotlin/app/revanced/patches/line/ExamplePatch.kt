package app.revanced.patches.line.spoof

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import java.security.MessageDigest

@Patch(
    name = "LINE Package Spoof",
    description = "LINEのパッケージ情報と署名を偽装するパッチ",
    compatiblePackages = [app.revanced.patcher.patch.CompatiblePackage("jp.naver.line.android")]
)
object LinePackageSpoofPatch : BytecodePatch() {

    private const val TARGET_PACKAGE = "jp.naver.line.android"
    private const val SPOOFED_SIGNATURE = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2"

    override fun execute(context: BytecodeContext) {
        // パッケージ名偽装処理
        context.classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                method.implementation?.instructions?.forEachIndexed { index, instruction ->
                    if (instruction.opcode == Opcode.CONST_STRING) {
                        val string = (instruction as ReferenceInstruction).reference.toString()
                        if (string.startsWith("jp.naver.line")) {
                            (method as MutableMethod).replaceInstruction(
                                index,
                                "const-string v0, \"$TARGET_PACKAGE\""
                            )
                        }
                    }
                }
            }
        }

        // 署名検証バイパス処理
        context.findClass("Ljp/naver/line/android/util/SignatureVerifier;")?.mutableClass?.methods
            ?.firstOrNull { it.name == "verifySignature" }
            ?.apply {
                implementation = implementation?.apply {
                    instructions.clear()
                    addInstruction("const/4 v0, 0x1")
                    addInstruction("return v0")
                }
            }

        // PackageManagerメソッドフック
        context.findClass("Landroid/content/pm/PackageManager;")?.mutableClass?.methods?.forEach { method ->
            when (method.name) {
                "getPackageInfo" -> method.addInstructions(
                    0,
                    """
                    invoke-static {p1}, ${this::class.java.name.replace('.', '/')}->spoofPackageName(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                    """
                )
                "getPackageSignatures" -> method.addInstructions(
                    0,
                    """
                    invoke-static {p0}, ${this::class.java.name.replace('.', '/')}->spoofSignature([B)[B
                    """
                )
            }
        }
    }

    @JvmStatic
    fun spoofPackageName(originalName: String): String {
        return if (originalName.startsWith("jp.naver.line")) TARGET_PACKAGE else originalName
    }

    @JvmStatic
    fun spoofSignature(signatures: ByteArray): ByteArray {
        return try {
            if (sha256(signatures) != SPOOFED_SIGNATURE) {
                // 実際の実装では正規の署名データを復号して返す
                signatures
            } else {
                signatures
            }
        } catch (e: Exception) {
            signatures
        }
    }

    private fun sha256(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }
}