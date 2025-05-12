package moe.lar.noki;

import moe.lar.noki.GlobalVars;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BaseHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam packageParam) {
        if (packageParam.packageName.equals("com.android.nfc")) {
            ClassLoader classLoader = packageParam.classLoader;
            Class <?> NfcService = XposedHelpers.findClassIfExists("com.android.nfc.NfcService", classLoader);

            if (NfcService == null) {
                XposedBridge.log(GlobalVars.TAG + "Failed to get target class");
                return;
            }

            XposedBridge.log(GlobalVars.TAG + " Start hooking.");

            XposedHelpers.findAndHookMethod(
                    NfcService,
                    "maybeDisconnectTarget",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                                String callmethod = element.getMethodName();
                                if (callmethod.equals("setReaderMode") || callmethod.equals("notifyTagAbort")) {
                                    XposedBridge.log("Skipping maybeDisconnectTarget call from " + callmethod);
                                    param.setResult(null);
                                    return;
                                }
                            }
                        }
                    }
            );

            XposedBridge.log(GlobalVars.TAG + " Hook success.");
        }
    }
}