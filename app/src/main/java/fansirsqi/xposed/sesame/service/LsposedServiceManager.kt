package fansirsqi.xposed.sesame.service

import fansirsqi.xposed.sesame.util.Log

/**
 * LsposedServiceManager stub — API 52 mode, no XposedService support.
 */
object LsposedServiceManager {

    private const val TAG = "LsposedServiceManager"

    val connectionState: ConnectionState
        get() = ConnectionState.Disconnected

    val isModuleActivated: Boolean
        get() = false

    fun init() {
        Log.record(TAG, "API 52 mode — LsposedService not available.")
    }

    fun addConnectionListener(listener: (ConnectionState) -> Unit) {
        listener(ConnectionState.Disconnected)
    }

    fun removeConnectionListener(listener: (ConnectionState) -> Unit) {
        // no-op
    }
}

sealed interface ConnectionState {
    data object Connecting : ConnectionState
    data object Disconnected : ConnectionState
}
