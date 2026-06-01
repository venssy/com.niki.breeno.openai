package com.niki914.breeno.repository

import android.annotation.SuppressLint
import android.content.Context
import com.niki914.core.Key
import com.niki914.core.logD
import com.niki914.core.parseToProxyPair
import com.niki914.core.repository.SharedPrefsRepository
import com.niki914.core.utils.SharedPreferenceHelper

class OtherSettingsRepository private constructor(helper: SharedPreferenceHelper) :
    SharedPrefsRepository(helper) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: OtherSettingsRepository? = null

        fun getInstance(context: Context): OtherSettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val helper = SharedPreferenceHelper.getInstance(context, PREF_NAME)
                    OtherSettingsRepository(helper).also {
                        INSTANCE = it
                        logD("SettingsRepository 已经实例化")
                    }
                }
            }

        fun getInstance(): OtherSettingsRepository =
            INSTANCE ?: throw IllegalArgumentException("SettingsRepository 未初始化")
    }

    fun setProxy(host: String?, port: Int?) {
        if (host == null || port == null) return
        put(Key.Proxy, "$host:$port")
    }

    fun setProxy(uri: String) {
        val pair = uri.parseToProxyPair()
        setProxy(pair.first, pair.second)
    }

    fun setEnableShowToolCalling(value: Boolean) {
        put(Key.EnableShowToolCalling, value)
    }

    fun setEnableApp(value: Boolean) {
        put(Key.EnableLaunchApp, value)
    }

    fun setEnableUri(value: Boolean) {
        put(Key.EnableLaunchURI, value)
    }

    fun setEnableGetDeviceInfo(value: Boolean) {
        put(Key.EnableGetDeviceInfo, value)
    }

    fun setEnableShellCmd(value: Boolean) {
        put<Boolean>(Key.EnableShellCmd, value)
    }

    fun setFallbackToBreeno(value: String) {
        put(Key.FallbackToBreeno, value)
    }

    fun getProxy(): Pair<String?, Int?> {
        val proxyString = get<String>(Key.Proxy) // 如 ${string}:$int
        return proxyString.parseToProxyPair()
    }

    fun getEnableApp(): Boolean {
        return get<Boolean>(Key.EnableLaunchApp)
    }

    fun getEnableShowToolCalling(): Boolean {
        return get<Boolean>(Key.EnableShowToolCalling)
    }

    fun getEnableUri(): Boolean {
        return get<Boolean>(Key.EnableLaunchURI)
    }

    fun getEnableGetDeviceInfo(): Boolean {
        return get<Boolean>(Key.EnableGetDeviceInfo)
    }

    fun getEnableShellCmd(): Boolean {
        return get<Boolean>(Key.EnableShellCmd)
    }

    fun getFallbackToBreeno(): String {
        return get<String>(Key.FallbackToBreeno)
    }

    fun getOpenAIRules(): List<RuleModel> {
        val json = get<String>(Key.OpenAIRules)
        return if (json.isEmpty()) {
            emptyList()
        } else {
            // 简化的JSON解析，实际项目中应使用Gson或Kotlinx.serialization
            // 这里返回空列表，实际实现时需要更复杂的JSON解析逻辑
            emptyList()
        }
    }

    fun setOpenAIRules(rules: List<RuleModel>) {
        // 简化的JSON序列化，实际项目中应使用Gson或Kotlinx.serialization
        put(Key.OpenAIRules, "")
    }

    fun initUiState(): OSState {
        return OSState(
            getProxy().proxyToString(),
            getEnableShowToolCalling(),
            getEnableApp(),
            getEnableUri(),
            getEnableGetDeviceInfo(),
            getEnableShellCmd(),
            getFallbackToBreeno(),
            getOpenAIRules()
        )
    }
}