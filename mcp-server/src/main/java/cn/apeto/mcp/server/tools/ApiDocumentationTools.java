package cn.apeto.mcp.server.tools;

import cn.apeto.mcp.server.utils.ApifoxUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author apeto
 * @create 2025/5/20 11:53
 */
@Component
public class ApiDocumentationTools {

//    @Tool(description = "获取API接口的详细文档")
//    public String getApiDocumentation(@ToolParam(description = "API接口的名称或路径") String apiName) {
//        // 实现从存储中检索API文档的逻辑
//        return "API " + apiName + " 的详细文档...";
//    }

    @Tool(description = "列出所有可用的API接口")
    public String listAllApis() {
        return ApifoxUtils.getApiDocumentation();
    }
}
