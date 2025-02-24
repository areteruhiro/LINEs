package app.revanced.patches.line

import app.revanced.patcher.patch.Option
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.line.util.Constants.LINE_PACKAGE_NAME
import app.revanced.patches.line.util.Constants.REVANCED_LINE_PACKAGE_NAME
import app.revanced.patches.line.util.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.line.util.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.patches.line.util.fingerprints.PrimeMethodFingerprint
import app.revanced.patches.line.misc.settings.PreferenceScreen
import app.revanced.patches.line.misc.settings.settingsPatch

@Suppress("unused")
val lineGmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = LINE_PACKAGE_NAME,
    toPackageName = REVANCED_LINE_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        CastContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = MainActivityOnCreateFingerprint,
    extensionPatch = lineExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::lineGmsCoreSupportResourcePatch,
) {
    dependsOn(
        disableUnsupportedFeaturesPatch, // LINE固有の非対応機能を無効化
        spoofMessageStreamsPatch,
    )

    compatibleWith(
        LINE_PACKAGE_NAME(
            "13.5.0",
            "13.6.1",
            "14.0.0",
            "14.1.1",
            "14.2.0"
        ),
    )
}

private fun lineGmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
    fromPackageName = LINE_PACKAGE_NAME,
    toPackageName = REVANCED_LINE_PACKAGE_NAME,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    spoofedPackageSignature = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2",
    executeBlock = {
        addResources("line", "misc.gms.gmsCoreSupportResourcePatch")

        val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

        PreferenceScreen.ADVANCED.addPreferences(
            IntentPreference(
                "microg_settings",
                intent = IntentPreference.Intent(
                    action = "android.intent.action.VIEW",
                    targetClass = "org.microg.gms.ui.SettingsActivity",
                    targetPackage = "$gmsCoreVendorGroupId.android.gms"
                )
            ),
            IntentPreference(
                "safetynet_check",
                intent = IntentPreference.Intent(
                    action = "android.intent.action.VIEW",
                    targetClass = "org.microg.gms.snet.SafetyNetTestActivity",
                    targetPackage = "$gmsCoreVendorGroupId.android.gms"
                )
            )
        )
    },
) {
    dependsOn(settingsPatch, addResourcesPatch)
}

// Constants.kt
object Constants {
    const val LINE_PACKAGE_NAME = "jp.naver.line.android"
    const val REVANCED_LINE_PACKAGE_NAME = "app.revanced.line"
    
    // LINE固有の定数
    const val LINE_SIGNATURE_HASH = "e682fe0bcd60907dfed515e0b8a4de03aa1c281d111a07833986602b6098afd2"
    const val LINE_PERMISSION_PREFIX = "jp.naver.line.permission"
    const val LINE_CONTENT_AUTHORITY = "jp.naver.line.provider"
}

// Fingerprints.kt
object PrimeMethodFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf(LINE_PACKAGE_NAME)
)

object CastContextFetchFingerprint : MethodFingerprint(
    returnType = "Lcom/google/android/gms/cast/framework/CastContext;",
    accessFlags = AccessFlags.PUBLIC,
    parameters = emptyList()
)

object MainActivityOnCreateFingerprint : MethodFingerprint(
    className = "Ljp/naver/line/android/MainActivity;",
    accessFlags = AccessFlags.PUBLIC,
    parameters = listOf("Landroid/os/Bundle;")
)
