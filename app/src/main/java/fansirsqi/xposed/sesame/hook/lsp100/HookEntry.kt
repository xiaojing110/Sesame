package fansirsqi.xposed.sesame.hook.lsp100

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

/**
 * 双重兼容入口：
 * - 新版 LSPosed → 通过 XposedModule 无参构造 + onModuleLoaded/onPackageLoaded
 * - 旧版 LSPosed → 通过 IXposedHookLoadPackage.handleLoadPackage
 */
class HookEntry : XposedModule(), IXposedHookLoadPackage {
    val tag = "LsposedEntry"
    private var processName: String? = null
    private var customHooker: ApplicationHook? = null

    // ===== 新版 LSPosed (libxposed API 101) =====

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        processName = param.processName
        if (customHooker == null) {
            customHooker = ApplicationHook()
        }
        XposedBridge.log("$tag: onModuleLoaded for process $processName")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        try {
            if (General.PACKAGE_NAME != param.packageName) return
            XposedEnv.classLoader = param.defaultClassLoader
            XposedEnv.appInfo = param.applicationInfo
            XposedEnv.packageName = param.packageName
            XposedEnv.processName = processName ?: ""
            customHooker = customHooker ?: ApplicationHook()
            customHooker?.loadPackage(param, processName)
            XposedBridge.log("$tag: [new-api] Hooking ${param.packageName} in process $processName")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: [new-api] Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }

    // ===== 旧版 LSPosed (Xposed API 82) =====

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            if (General.PACKAGE_NAME != lpparam.packageName) return
            XposedEnv.classLoader = lpparam.classLoader
            XposedEnv.appInfo = lpparam.appInfo
            XposedEnv.packageName = lpparam.packageName
            XposedEnv.processName = lpparam.processName

            customHooker = customHooker ?: ApplicationHook()
            customHooker?.loadPackageCompat(lpparam)

            XposedBridge.log("$tag: [old-api] Hooking ${lpparam.packageName} in process ${lpparam.processName}")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: [old-api] Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }
}
