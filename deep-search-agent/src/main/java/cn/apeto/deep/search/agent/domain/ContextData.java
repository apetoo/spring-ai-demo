package cn.apeto.deep.search.agent.domain;

import lombok.Data;

import java.util.List;

/**
 * 研究上下文数据
 */
@Data
public class ContextData {

    /**
     * 用户查询
     */
    private String query;

    /**
     * 报告结构JSON
     */
    private String reportStructure;

    /**
     * 段落列表
     */
    private List<ReportStructureEntity> paragraphs;

    /**
     * 最终报告
     */
    private String finalReport;
}
