package com.kama.jchatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 多聊天模型配置类，提供不同聊天模型的ChatClient Bean
// 通过@Bean注解创建不同聊天模型的ChatClient实例，分别命名为"deepseek-chat"和"glm-4.6"，以便在应用中区分使用
//ChatClient是Spring AI提供的一个接口，用于与不同聊天模型进行交互，封装了发送消息和接收回复的逻辑
@Configuration
public class MultiChatClientConfig {
    // deepseek
    @Bean("deepseek-chat")//bean是spring框架中的一个核心概念，表示一个由spring容器管理的对象，通过@Bean注解标注的方法会被spring容器调用，并将返回的对象注册为一个bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.create(deepSeekChatModel);
    }

    // zhipuai
    @Bean("glm-4.6")
    public ChatClient zhiPuAiChatClient(ZhiPuAiChatModel zhiPuAiChatModel) {
        return ChatClient.create(zhiPuAiChatModel);
    }
}
