package fansirsqi.xposed.sesame.hook.lsp100

import de.robv.android.xposed.XposedBridge
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

/**
 * LSPosed API 101 入口
 * 对应旧版 fansirsqi.xposed.sesame.hook.xp82.HookEntry
 *
 * API 101 变更:
 * - XposedModule 不再通过构造函数传入 XposedInterface，改为由框架调用 attachFramework()
 * - 入口改为先回调 onModuleLoaded()，再回调 onPackageLoaded()
 * - PackageLoadedParam 使用 getDefaultClassLoader() 获取 ClassLoader
 */
class HookEntry : XposedModule() {

    private val tag = "Lsp101Entry"
    private var processName: String? = null
    var customHooker: ApplicationHook? = null

    /**
     * 模块首次加载到进程时回调（每个进程仅一次）
     */
    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        processName = param.processName
        customHooker = ApplicationHook()
        customHooker?.xposedInterface = this as XposedInterface

        XposedBridge.log("$tag: Initialized for process $processName")

        val baseFw = "${frameworkName} $frameworkVersion $frameworkVersionCode target_process: $processName"
        XposedBridge.log("$tag: Framework: $baseFw")
    }

    /**
     * 当模块作用域内的应用进程启动时，框架会回调此方法。
     */
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        try {
            if (General.PACKAGE_NAME != param.packageName) return
            // API 101: PackageLoadedParam 使用 getDefaultClassLoader() 获取 ClassLoader
            XposedEnv.classLoader = param.defaultClassLoader
            XposedEnv.appInfo = param.applicationInfo
            XposedEnv.packageName = param.packageName
            XposedEnv.processName = processName ?: param.packageName
            customHooker?.loadPackage(param)
            XposedBridge.log("$tag: Hooking ${param.packageName} in process $processName")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }
}
