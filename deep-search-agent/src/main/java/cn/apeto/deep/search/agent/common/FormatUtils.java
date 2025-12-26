package cn.apeto.deep.search.agent.common;

import cn.apeto.deep.search.agent.client.response.TavilySearchResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author apeto
 * @create 2025/12/25 09:50
 */
public class FormatUtils {

    public static List<String> searchResultForPrompt(List<TavilySearchResponse.SearchResult> results, int maxLength) {
        // 截断
        List<String> truncatedResults = new ArrayList<>();
        if (results == null) {
            return truncatedResults;
        }
        for (TavilySearchResponse.SearchResult result : results) {
            String content = result.getContent();
            if (content.length() > maxLength) {
                truncatedResults.add(content.substring(0, maxLength));
            } else {
                truncatedResults.add(content);
            }
        }
        return truncatedResults;
    }
}
