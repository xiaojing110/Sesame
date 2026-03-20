package fansirsqi.xposed.sesame.hook.lsp100

import de.robv.android.xposed.XposedBridge
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry : XposedModule() {
    val tag = "LsposedEntry"
    private var processName: String? = null
    var customHooker: ApplicationHook? = null

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        processName = param.processName
        customHooker = ApplicationHook()
        XposedBridge.log("$tag: Initialized for process $processName")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        try {
            if (General.PACKAGE_NAME != param.packageName) return
            XposedEnv.classLoader = param.defaultClassLoader
            XposedEnv.appInfo = param.applicationInfo
            XposedEnv.packageName = param.packageName
            XposedEnv.processName = processName
            customHooker?.loadPackage(param, processName)
            XposedBridge.log("$tag: Hooking ${param.packageName} in process $processName")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }
}
