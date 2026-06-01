package com.niki914.core.model

/**
 * OpenAI规则模型，用于存储正则匹配规则和对应的参数替换配置
 */
data class RuleModel(
    val id: String = "", // 规则唯一标识
    val regex: String = "", // 正则表达式
    val headers: Map<String, String> = emptyMap(), // 要替换或添加的header参数
    val body: Map<String, Any> = emptyMap() // 要替换或添加的body参数
) {

    /**
     * 创建一个默认的空规则
     */
    companion object {
        fun empty(): RuleModel {
            return RuleModel()
        }
    }
}