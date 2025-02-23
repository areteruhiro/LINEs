package app.revanced.patches.line.spoof

import android.content.pm.PackageManager
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import java.security.MessageDigest

@Patch(
    name = "LINE Package Spoof",
    description = "LINEのパッケージ情報と署名を偽装するパッチ",
    compatiblePackages = [CompatiblePackage("jp.naver.line.android")]
)
object LinePackageSpoofPatch : BytecodePatch() {

    // 定数定義
    private const val SPOOFED_PACKAGE_NAME = "jp.naver.line.android"
    private const val OFFICIAL_SIGNATURE_SHA256 = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2"

    override fun execute(context: BytecodeContext) {
        spoofPackageNames(context)
        bypassSignatureVerification(context)
        hookPackageManagerMethods(context)
    }

    private fun spoofPackageNames(context: BytecodeContext) {
        context.classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                method.implementation?.instructions?.forEachIndexed { index, instruction ->
                    if (instruction.opcode == Opcode.CONST_STRING) {
                        val string = (instruction as ReferenceInstruction).reference.toString()
                        if (string.startsWith("jp.naver.line")) {
                            method.replaceInstruction(
                                index,
                                "const-string v0, \"$SPOOFED_PACKAGE_NAME\""
                            )
                        }
                    }
                }
            }
        }
    }

    private fun bypassSignatureVerification(context: BytecodeContext) {
        context.findClass("Ljp/naver/line/android/util/SignatureVerifier;")?.let { verifierClass ->
            verifierClass.methods
                .filter { it.name == "verifySignature" }
                .forEach { method ->
                    method.implementation?.instructions?.apply {
                        clear()
                        add(
                            """
                            const/4 v0, 0x1
                            return v0
                            """.trimIndent()
                        )
                    }
                }
        }
    }

    private fun hookPackageManagerMethods(context: BytecodeContext) {
        context.findClass("Landroid/content/pm/PackageManager;")?.methods?.forEach { method ->
            when (method.name) {
                "getPackageInfo" -> method.addInstructions(
                    0,
                    """
                    invoke-static {p1}, ${this::class.java.name.replace('.', '/')}->spoofPackageName(Landroid/content/pm/PackageManager;Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                    """
                )
                "getPackageSignatures" -> method.addInstructions(
                    0,
                    """
                    invoke-static {p0}, ${this::class.java.name.replace('.', '/')}->spoofSignature([Landroid/content/pm/Signature;)[Landroid/content/pm/Signature;
                    """
                )
            }
        }
    }

    @JvmStatic
    fun spoofPackageName(pm: PackageManager, originalName: String): String {
        return if (originalName.startsWith("jp.naver.line")) {
            SPOOFED_PACKAGE_NAME.also {
                println("[Spoof] Package name changed: $originalName -> $it")
            }
        } else {
            originalName
        }
    }

    @JvmStatic
    fun spoofSignature(signatures: ByteArray): ByteArray {
        return try {
            val currentHash = sha256(signatures)
            if (currentHash == OFFICIAL_SIGNATURE_SHA256) {
                signatures
            } else {
                // 実際の実装では正規の署名データを返す
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