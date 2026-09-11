package com.lumi.servicespatcher;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Bypasses protected-package and protected-permission-prefix checks in system_server
 * (services.jar / PackageManagerService + ProtectedPermissionPrefixList).
 *
 * Patch 1 – ProtectedPermissionPrefixList.isInProtectedPrefix(String)
 *   Always returns false so no permission string is ever blocked for having a protected prefix.
 *
 * Patch 2 – ProtectedPackages.isPackageStateProtected(int, String)
 *   Always returns false so PackageManagerService never throws SecurityException when
 *   disabling a package it considers "protected" (covers every call-site in system_server,
 *   not just setEnabledSettings).
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "ServicesPatcher";

    private static final String PROTECTED_PREFIX_LIST_CLASS =
            "com.android.server.pm.permission.ProtectedPermissionPrefixList";
    private static final String PROTECTED_PACKAGES_CLASS =
            "com.android.server.pm.ProtectedPackages";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        if (!"android".equals(lpparam.packageName)) return;

        Log.i(TAG, "Loaded into system_server (pid=" + android.os.Process.myPid() + ")");

        hookProtectedPermissionPrefixList(lpparam);
        hookProtectedPackages(lpparam);
    }

    /**
     * ProtectedPermissionPrefixList.isInProtectedPrefix(String) -> false
     *
     * Original iterates mPrefixes and returns true if the given permission starts with
     * any protected prefix. Replaced entirely to always return false, allowing apps to
     * declare or use permissions with otherwise-protected prefixes.
     */
    private void hookProtectedPermissionPrefixList(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    PROTECTED_PREFIX_LIST_CLASS,
                    lpparam.classLoader,
                    "isInProtectedPrefix",
                    String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            Log.d(TAG, "isInProtectedPrefix(\"" + param.args[0] + "\") -> false");
                            return false;
                        }
                    });
            Log.i(TAG, "Hooked " + PROTECTED_PREFIX_LIST_CLASS + ".isInProtectedPrefix");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to hook " + PROTECTED_PREFIX_LIST_CLASS + ".isInProtectedPrefix", t);
        }
    }

    /**
     * ProtectedPackages.isPackageStateProtected(int userId, String packageName) -> false
     *
     * Original checks whether a package is in the protected-packages list for a given user.
     * PackageManagerService.setEnabledSettings() calls this and throws SecurityException
     * ("Cannot disable a protected package: ...") when it returns true. Replaced entirely
     * to always return false, letting any package be disabled regardless of protected status.
     *
     * Hooking at this level rather than inside setEnabledSettings covers every call-site
     * across system_server, making the patch version-agnostic.
     */
    private void hookProtectedPackages(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    PROTECTED_PACKAGES_CLASS,
                    lpparam.classLoader,
                    "isPackageStateProtected",
                    int.class,
                    String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            Log.d(TAG, "isPackageStateProtected(userId=" + param.args[0]
                                    + ", pkg=\"" + param.args[1] + "\") -> false");
                            return false;
                        }
                    });
            Log.i(TAG, "Hooked " + PROTECTED_PACKAGES_CLASS + ".isPackageStateProtected");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to hook " + PROTECTED_PACKAGES_CLASS + ".isPackageStateProtected", t);
        }
    }
}
