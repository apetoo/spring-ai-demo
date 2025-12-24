package cn.apeto.deep.search.agent.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 报告段落实体
 */
@Schema(description = "深度搜索结构化输出")
@Data
public class ReportStructureEntity {

    /**
     * 段落标题
     */
    private String title;

    /**
     * 段落预期内容描述
     */
    private String content;

    /**
     * 第一次搜索查询
     */
    private String searchQuery;

    /**
     * 第一次搜索推理
     */
    private String searchReasoning;

    /**
     * 第一次搜索结果
     */
    private List<String> searchResults;

    /**
     * 段落当前状态
     */
    private String latestState;

    /**
     * 反思查询
     */
    private String reflectionQuery;

    /**
     * 反思推理
     */
    private String reflectionReasoning;

    /**
     * 反思搜索结果
     */
    private List<String> reflectionSearchResults;


    private List<SearchResult> searchResultList;
}
