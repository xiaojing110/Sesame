package fansirsqi.xposed.sesame.hook.xp82

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fansirsqi.xposed.sesame.data.General
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.hook.XposedEnv

/**
 * ⚠️ 已废弃 (Deprecated) - 仅作为旧版框架最后兜底
 * 旧版 Xposed 框架（如 EdXposed、LSPosed < 1.9）入口
 * 新版本请使用:
 *   - API 89/100: fansirsqi.xposed.sesame.hook.lsp89.HookEntry
 *   - API 101:    fansirsqi.xposed.sesame.hook.lsp100.HookEntry
 */
@Deprecated("API 82 已废弃，请升级到 LSPosed 1.9+ (API 89+)")
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
