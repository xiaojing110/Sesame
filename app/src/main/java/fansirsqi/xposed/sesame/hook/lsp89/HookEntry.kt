package fansirsqi.xposed.sesame.hook.lsp89

import de.robv.android.xposed.XposedBridge
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

/**
 * LSPosed API 89/100 入口
 * 对应 LSPosed 1.9.x (API 89) 和早期 1.10.x (API 100)
 *
 * API 89/100 特征:
 * - XposedModule 通过构造函数传入 XposedInterface (1-arg)
 * - PackageLoadedParam 使用 classLoader 获取 ClassLoader
 * - 初始化在构造函数中完成
 *
 * 编译依赖: libs/api-100.aar
 */
class HookEntry(base: XposedInterface) : XposedModule(base) {

    private val tag = "Lsp89Entry"
    var customHooker: ApplicationHook? = null

    init {
        customHooker = ApplicationHook()
        customHooker?.xposedInterface = base

        XposedBridge.log("$tag: Initialized")

        try {
            val baseFw = "${base.frameworkName} ${base.frameworkVersion} ${base.frameworkVersionCode}"
            XposedBridge.log("$tag: Framework: $baseFw")
        } catch (_: Throwable) {
        }
    }

    /**
     * 当模块作用域内的应用进程启动时，框架会回调此方法。
     */
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        try {
            if (General.PACKAGE_NAME != param.packageName) return
            // API 89/100: PackageLoadedParam 使用 classLoader 获取 ClassLoader
            XposedEnv.classLoader = param.classLoader
            XposedEnv.appInfo = param.applicationInfo
            XposedEnv.packageName = param.packageName
            // API 89/100 PackageLoadedParam 没有 processName，使用 packageName 作为 fallback
            XposedEnv.processName = param.packageName
            customHooker?.loadPackage(param)
            XposedBridge.log("$tag: Hooking ${param.packageName}")
        } catch (e: Throwable) {
            XposedBridge.log("$tag: Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }
}
