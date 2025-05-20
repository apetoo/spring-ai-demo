package cn.apeto.springmcpdemo.config;

import cn.apeto.springmcpdemo.tools.ApiDocumentationTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author apeto
 * @create 2025/5/20 11:53
 */
@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallbackProvider weatherTools(ApiDocumentationTools apiDocumentationTools) {
        return MethodToolCallbackProvider.builder().toolObjects(apiDocumentationTools).build();
    }
}
