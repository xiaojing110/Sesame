package fansirsqi.xposed.sesame.hook.lsp101

import de.robv.android.xposed.XposedBridge
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry(
    base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam
) : XposedModule(base, param) {
    val tag = "Lsp101Entry"
    private var processName: String = param.processName
    var customHooker: ApplicationHook? = null

    init {
        customHooker = ApplicationHook()
        customHooker?.xposedInterface = base
        XposedBridge.log("$tag: Initialized for process $processName")

        val baseFw = "${base.frameworkName} ${base.frameworkVersion} ${base.frameworkVersionCode} target_model_process: ${processName}"
        XposedBridge.log("$tag: Framework from base: $baseFw")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        try {
            if (General.PACKAGE_NAME != param.packageName) return
            XposedEnv.classLoader = param.defaultClassLoader
            XposedEnv.appInfo = param.applicationInfo
            XposedEnv.packageName = param.packageName
            XposedEnv.processName = processName
            customHooker?.loadPackage(
                param.defaultClassLoader,
                param.packageName,
                param.applicationInfo.sourceDir,
                processName
            )
            XposedBridge.log("$tag: Hooking ${param.packageName} in process $processName")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }
}
