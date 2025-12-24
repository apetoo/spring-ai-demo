package cn.apeto.deep.search.agent.domain;

import lombok.Data;

/**
 * @author apeto
 * @create 2025/12/17 18:08
 */
@Data
public class LLMSearchResponse {

    private String search_query;
    private String reasoning;
}
