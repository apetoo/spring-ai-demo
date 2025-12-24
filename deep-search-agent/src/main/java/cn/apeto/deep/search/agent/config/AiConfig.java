package cn.apeto.deep.search.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author apeto
 * @create 2025/5/20 18:02
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        DeepSeekChatOptions.Builder builder = DeepSeekChatOptions.builder()
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormat.Type.JSON_OBJECT).build());
        chatClientBuilder.defaultOptions(builder.build());
        return chatClientBuilder.build();
    }

    @Bean
    public ChatClient chatClientText(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
