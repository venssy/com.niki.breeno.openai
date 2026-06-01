package com.niki914.chat

import com.niki914.chat.ChatConfigBuilder.Companion.initialChatConfig
import com.niki914.chat.beans.ChatCompletionRequest
import com.niki914.chat.beans.ChatEvent
import com.niki914.chat.beans.Message
import com.niki914.chat.beans.ToolDefinition
import com.niki914.chat.inner.ChatConfig
import com.niki914.chat.inner.ChatStreamProcessor
import com.niki914.chat.inner.DynamicChatConfigHolder
import com.niki914.core.logD
import com.niki914.net.DynamicOkhttpClientManager
import kotlinx.coroutines.flow.Flow

// TODO 重构
class Chat(
    apiKey: String,
    modelName: String,
    prompt: String? = null,
    tools: List<ToolDefinition>? = null
) {
    private val processor by lazy { ChatStreamProcessor() }

    private val initialConfig = initialChatConfig(
        apiKey, modelName, prompt, tools
    )

    private val dynamicChatConfigHolder = DynamicChatConfigHolder(initialConfig)

    private val systemMessage: Message?
        get() {
            val config = getConfig()
            return if (config.prompt.isNullOrBlank())
                null
            else
                Message.System(config.prompt)
        }

    private val _history: MutableList<Message> = mutableListOf()
    val history: List<Message>
        get() = _history.toList()

    fun preConnect() = processor.preConnect()

//    fun exampleUpdateConfig() {
//        updateConfig {
//            apiKey = ""
//            modelName = ""
//            prompt = null
//            tools = listOf<ToolDefinition>(
//                function(
//                    name = "getCurrentWeather",
//                    description = "天气查询",
//                    parameters = FunctionParameters(
//                        type = "object",
//                        properties = mapOf(
//                            "location" to PropertyDefinition(
//                                type = "string",
//                                description = "城市名，例如：北京"
//                            )
//                        ),
//                        required = listOf("location") // 明确指定 required 参数
//                    )
//                ) as ToolDefinition
//            )
//            network {
//                baseUrl = "https://a.com"
//                httpProxy("1.1.1.1", 8080)
//            }
//        }
//    }

    fun updateConfig(block: ChatConfigBuilder.() -> Unit) {
        val oldConfig = dynamicChatConfigHolder.getConfig() // 获取旧配置
        dynamicChatConfigHolder.update(block) // 更新 ChatConfig

        // 获取新的 ChatConfig
        val newConfig = dynamicChatConfigHolder.getConfig()

        // 如果 NetworkConfig 发生变化，则同步更新 DynamicRetrofitManager
        if (oldConfig.netConfig != newConfig.netConfig) {
            DynamicOkhttpClientManager.updateConfig(newConfig.netConfig)
        }
    }

    fun append(vararg message: Message) {
        _history.addAll(message)
    }

    fun clear() {
        _history.clear()
    }

    suspend fun sendMessages(messages: List<Message>): Flow<ChatEvent> {
        val config = getConfig()
        val modifiedConfig = applyRules(messages.joinToString(""), config)
        return streaming(listOf(systemMessage) + messages, modifiedConfig)
    }

    suspend fun sendMessage(vararg message: Message? = arrayOf(null)): Flow<ChatEvent> {
        message.toList().forEach {
            it?.let { msg ->
                append(msg)
            }
        }
        val config = getConfig()
        val modifiedConfig = applyRules(_history.joinToString(""), config)
        return streaming(listOf(systemMessage) + _history.toList(), modifiedConfig)
    }

    private suspend fun streaming(
        messages: List<Message?>,
        config: ChatConfig = getConfig()
    ): Flow<ChatEvent> {
        logD(config.toString())
        return processor.streaming( // service.chat 是安全的，内部有异常捕捉
            config.apiKey,
            ChatCompletionRequest(
                model = config.modelName,
                messages = messages.filterNotNull(),
                tools = config.tools
            )
        )
    }

    private fun getConfig(): ChatConfig {
        return dynamicChatConfigHolder.getConfig()
    }

    // 用于应用规则的函数
    fun applyRules(text: String, config: ChatConfig): ChatConfig {
        // 获取OpenAI规则列表
        val rules = getOpenAIRulesFromSettings()

        // 遍历所有规则进行匹配
        for (rule in rules) {
            if (Regex(rule.regex).containsMatchIn(text)) {
                // 应用Header替换
                val headers = mutableMapOf<String, String>()
                headers.putAll(config.netConfig.headers)
                headers.putAll(rule.headers)

                // 应用Body替换
                val model = rule.body["model"] as? String ?: config.modelName
                val modifiedBody = mutableMapOf<String, Any>()
                modifiedBody.putAll(rule.body)
                modifiedBody["model"] = model

                // 创建新的配置并返回
                return config.copy(
                    netConfig = config.netConfig.copy(headers = headers),
                    modelName = model
                )
            }
        }

        // 没有匹配到任何规则，返回原始配置
        return config
    }

    // 从设置中获取OpenAI规则列表的辅助方法
    private fun getOpenAIRulesFromSettings(): List<RuleModel> {
        // 在实际项目中，应该从ViewModel或Repository中获取规则
        // 由于当前架构限制，这里返回空列表作为占位
        // 在实际实现中，需要通过依赖注入或其他方式获取规则
        return emptyList()
    }