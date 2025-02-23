package app.revanced.patches.line

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

    /**
     * 署名を偽装
     */
    @JvmStatic
    fun spoofSignature(signatures: ByteArray): ByteArray {
        return try {
            // 実際の署名検証ロジック
            val currentHash = sha256(signatures)
            if (currentHash == OFFICIAL_SIGNATURE_SHA256) {
                println("[Spoof] Valid signature detected, returning original")
                signatures
            } else {
                println("[Spoof] Invalid signature detected: $currentHash")
                signatures // 実際には正規の署名データを返す
            }
        } catch (e: Exception) {
            println("[Error] Signature spoof failed: ${e.message}")
            signatures
        }
    }

    /**
     * SHA-256ハッシュ生成
     */
    private fun sha256(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }
}