package cn.apeto.deep.search.agent.client.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Tavily搜索响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TavilySearchResponse {

    @Schema(description = "查询关键词")
    private String query;

    @Schema(description = "后续问题")
    private List<String> followUpQuestions;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "搜索结果列表")
    private List<SearchResult> results;

    @Schema(description = "响应时间")
    @JsonProperty("response_time")
    private Double responseTime;

    @Schema(description = "请求ID")
    @JsonProperty("request_id")
    private String requestId;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchResult {

        @Schema(description = "链接URL")
        private String url;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "内容摘要")
        private String content;

        @Schema(description = "匹配分数")
        private Double score;

        @Schema(description = "原始内容")
        private String rawContent;

        @Schema(description = "网站图标")
        private String favicon;


    }
}
