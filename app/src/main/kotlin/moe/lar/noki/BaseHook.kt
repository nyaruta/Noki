package moe.lar.noki

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class BaseHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(packageParam: XC_LoadPackage.LoadPackageParam) {
        if (packageParam.packageName == "com.android.nfc") {
            val classLoader = packageParam.classLoader
            val nfcService = XposedHelpers.findClassIfExists("com.android.nfc.NfcService", classLoader)

            if (nfcService == null) {
                XposedBridge.log("${GlobalVars.TAG} Failed to get target class")
                return
            }

            XposedBridge.log("${GlobalVars.TAG} Start hooking.")

            XposedHelpers.findAndHookMethod(
                nfcService,
                "maybeDisconnectTarget",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        Thread.currentThread().stackTrace.forEach { element ->
                            val callMethod = element.methodName
                            if (callMethod == "setReaderMode" || callMethod == "notifyTagAbort") {
                                XposedBridge.log("${GlobalVars.TAG} Skipping maybeDisconnectTarget call from $callMethod")
                                param.result = null
                                return
                            }
                        }
                    }
                }
            )

            XposedBridge.log("${GlobalVars.TAG} Hook success.")
        }
    }
}