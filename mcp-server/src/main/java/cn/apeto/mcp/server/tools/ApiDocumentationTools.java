package cn.apeto.springmcpdemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ApiDocumentationTools {

    @Tool(description = "获取API接口的详细文档")
    public String getApiDocumentation(@ToolParam(description = "API接口的名称或路径") String apiName) {
        // 实现从存储中检索API文档的逻辑
        return "API " + apiName + " 的详细文档...";
    }

    @Tool(description = "列出所有可用的API接口")
    public String listAllApis() {
        // 实现列出所有API的逻辑
        return "可用的API列表：...";
    }
}
