package fansirsqi.xposed.sesame.hook.xp82

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv

/**
 * 传统 Xposed 入口 (IXposedHookLoadPackage)
 * 兼容所有 LSPosed 版本 (API 82 / 89 / 100 / 101)
 * - API 101 框架优先加载 java_init.list → lsp100.HookEntry (XposedModule)
 * - API 89 及更早框架加载 xposed_init → 本类 (IXposedHookLoadPackage)
 */
class HookEntry : IXposedHookLoadPackage {

    private val tag = "Xp82Entry"
    private var customHooker: ApplicationHook? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 只在目标应用执行
            if (lpparam.packageName != General.PACKAGE_NAME) {
                return
            }
            // 只在主进程执行（防止子进程重复初始化）
            XposedEnv.classLoader = lpparam.classLoader
            XposedEnv.appInfo = lpparam.appInfo
            XposedEnv.packageName = lpparam.packageName
            XposedEnv.processName = lpparam.processName

            customHooker = ApplicationHook()

            XposedBridge.log("$tag: Hooking ${lpparam.packageName} in process ${lpparam.processName}")
            // 调用你自己的 Hook 逻辑
            customHooker?.loadPackageCompat(lpparam)

        } catch (e: Throwable) {
            XposedBridge.log("$tag: Hook failed - ${e.message}")
            XposedBridge.log(e)
        }
    }

}
