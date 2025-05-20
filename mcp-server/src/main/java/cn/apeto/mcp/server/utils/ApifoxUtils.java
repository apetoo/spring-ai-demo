package cn.apeto.mcp.server.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;

/**
 * @author apeto
 * @create 2025/5/20 16:57
 */
public class ApifoxUtils {

    public static String getApiDocumentation() {

        String url = "https://api.apifox.com/v1/projects/6423666/export-openapi?locale=zh-CN";

        return HttpRequest.post(url)
                .header("X-Apifox-Api-Version", "2024-03-28")
                .header("Authorization", "Bearer APS-xDNLSz9osZ4N2h57nFXz63uI3XpAQLGr")
                .body("{\n  \"scope\": {\n    \"type\": \"ALL\",\n    \"excludedByTags\": [\"pet\"]\n  },\n  \"options\": {\n    \"includeApifoxExtensionProperties\": false,\n    \"addFoldersToTags\": false\n  },\n  \"oasVersion\": \"3.1\",\n  \"exportFormat\": \"JSON\"\n}")
                .execute().body();
    }

    public static void main(String[] args) {

        System.out.println(StrUtil.trim(getApiDocumentation()));
    }
}
