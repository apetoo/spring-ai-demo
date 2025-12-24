package cn.apeto.deep.search.agent.domain;

import lombok.Data;

/**
 * @author apeto
 * @create 2025/12/24 14:13
 */
@Data
public class SearchResult {

    private String query;
    private String url;
    private String title;
    private String content;
    private Double score;
}
