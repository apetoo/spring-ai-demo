package cn.apeto.mcp.server.tools;

import cn.apeto.mcp.server.utils.ApifoxUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author apeto
 * @create 2025/5/20 11:53
 */
@Component
public class ApiDocumentationTools {

    @Value("classpath:/prompts/python-py-code-rule.st")
    private Resource pyCodeRule;
    @Value("classpath:/prompts/java-code-rule.st")
    private Resource javaCodeRule;

    @Tool(description = "列出所有可用的API接口")
    public String listAllApis() {
        return ApifoxUtils.getApiDocumentation();
    }

    @Tool(description = "接口调用公司规范")
    public String codeRule(@ToolParam(description = "开发语言 java or python") String language) throws IOException {
        return switch (language) {
            case "java" -> javaCodeRule.getContentAsString(Charset.defaultCharset());
            case "python" -> pyCodeRule.getContentAsString(Charset.defaultCharset());
            default -> "暂不支持该语言";
        };
    }
}
