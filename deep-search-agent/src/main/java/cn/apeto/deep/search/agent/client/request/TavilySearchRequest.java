package cn.apeto.deep.search.agent.client.request;

import lombok.Data;

/**
 * @author apeto
 * @create 2025/12/10 15:07
 */
@Data
public class TavilySearchRequest {

    private String query;
    private String max_results = "3";
    private Boolean include_raw_content = false;
}
