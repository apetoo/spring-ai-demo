package cn.apeto.deep.search.agent.service;

import cn.apeto.deep.search.agent.client.TavilySearchClient;
import cn.apeto.deep.search.agent.client.request.TavilySearchRequest;
import cn.apeto.deep.search.agent.client.response.TavilySearchResponse;
import cn.apeto.deep.search.agent.domain.*;
import cn.apeto.deep.search.agent.nodes.*;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 研究服务 - 编排深度搜索执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchService {

    private final ReportStructureNode reportStructureNode;
    private final FirstSearchNode firstSearchNode;
    private final FirstSummaryNode firstSummaryNode;
    private final ReflectionNode reflectionNode;
    private final ReflectionSummaryNode reflectionSummaryNode;
    private final ReportFormattingNode reportFormattingNode;
    private final TavilySearchClient tavilySearchClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行深度研究，返回流式输出
     * 使用混合模式：步骤间同步，步骤内异步
     */
    public Flux<String> research(String query) {
        ContextData contextData = new ContextData();
        contextData.setQuery(query);

        return Flux.concat(
                // 步骤1: 生成报告结构 (等待完成后再进行下一步)
                generateReportStructure(contextData),

                // 步骤2: 处理所有段落 (基于步骤1的结果)
                Flux.defer(() -> processAllParagraphs(contextData)),

                // 步骤3: 生成最终报告 (基于步骤2的结果)
                Flux.defer(() -> generateFinalReport(contextData))
        );
    }

    /**
     * 生成报告结构 - 等待完成后再继续
     */
    private Flux<String> generateReportStructure(ContextData context) {
        return Flux.concat(
                Flux.just("📋 正在生成报告结构...\n"),
                reportStructureNode.execute(context.getQuery(), context)
                        .collectList() // 等待LLM完成
                        .flatMapMany(chunks -> {
                            String result = String.join("", chunks);
                            List<ReportStructureEntity> structureList = JSONUtil.toBean(result, new TypeReference<>() {
                            }, false);
                            context.setParagraphs(structureList);
                            context.setReportStructure(result);

                            // 输出详细的报告结构信息
                            StringBuilder structureDetails = new StringBuilder();
                            structureDetails.append("REPORT_STRUCTURE_START\n");
                            structureDetails.append(String.format("段落总数: %d\n", structureList.size()));

                            for (int i = 0; i < structureList.size(); i++) {
                                ReportStructureEntity paragraph = structureList.get(i);
                                structureDetails.append(String.format("段落 %d: %s\n", i + 1, paragraph.getTitle()));
                                structureDetails.append(String.format("描述: %s\n", paragraph.getContent()));
                                structureDetails.append("---\n");
                            }
                            structureDetails.append("REPORT_STRUCTURE_END\n");
                            log.info("生成结构完成: {}", JSONUtil.toJsonStr(structureList));
                            return Flux.just(
                                    structureDetails.toString(),
                                    String.format("✅ 报告结构生成完成，共 %d 个段落\n\n", structureList.size())
                            );
                        })
        );
    }

    /**
     * 处理所有段落
     */
    @SuppressWarnings("unchecked")
    private Flux<String> processAllParagraphs(ContextData context) {
        List<ReportStructureEntity> paragraphs = context.getParagraphs();

        if (paragraphs == null || paragraphs.isEmpty()) {
            return Flux.just("❌ 没有段落需要处理\n");
        }

        // 为每个段落创建处理流
        List<Flux<String>> paragraphFluxes = new ArrayList<>();

        for (int i = 0; i < paragraphs.size(); i++) {
            ReportStructureEntity paragraph = paragraphs.get(i);

            Flux<String> paragraphFlux = processSingleParagraph(context, paragraph, i);
            paragraphFluxes.add(paragraphFlux);
        }

        // 顺序处理所有段落
        return Flux.concat(paragraphFluxes);
    }

    /**
     * 处理单个段落的深度搜索 - 确保步骤顺序执行
     */
    private Flux<String> processSingleParagraph(ContextData context,
                                                ReportStructureEntity paragraph,
                                                int index) {
        String title = paragraph.getTitle();

        return Flux.concat(
                // 段落开始
                Flux.just(String.format("🔄 段落 %d: %s\n", index + 1, title)),

                // 1. 生成搜索查询 (等待完成)
                generateSearchQuery(context, paragraph),

                // 2. 执行搜索 (基于步骤1的结果)
                Flux.defer(() -> executeSearch(context, paragraph)),

                // 3. 总结搜索结果 (基于步骤2的结果)
                Flux.defer(() -> summarizeSearchResults(context, paragraph)),

                // 4. 反思和丰富内容 (基于步骤3的结果)
                Flux.defer(() -> enrichParagraph(context, paragraph)),

                // 段落完成
                Flux.just(String.format("✅ 段落 %d 完成\n\n", index + 1))
        );
    }

    /**
     * 生成搜索查询 - 等待完成后更新段落状态
     */
    private Flux<String> generateSearchQuery(ContextData context,
                                             ReportStructureEntity paragraph) {
        // 构建输入JSON
        String inputJson = String.format("""
                {
                    "title": "%s",
                    "content": "%s"
                }
                """, paragraph.getTitle(), paragraph.getContent());

        return Flux.concat(
                Flux.just("  🔍 生成搜索查询... "),
                firstSearchNode.execute(inputJson, context)
                        .collectList()
                        .flatMapMany(chunks -> {
                            String result = String.join("", chunks);
                            try {
                                log.info("生成搜索参数: {}", inputJson);
                                LLMSearchResponse llmSearchResponse = JSONUtil.toBean(result, LLMSearchResponse.class);

                                // 同步更新段落状态
                                paragraph.setSearchQuery(llmSearchResponse.getSearch_query());
                                paragraph.setSearchReasoning(llmSearchResponse.getReasoning());
                                log.info("生成返回结果:{}", llmSearchResponse.getSearch_query());
                                return Flux.just("完成\n");
                            } catch (Exception e) {
                                log.error("解析搜索查询失败: {}", result, e);
                                return Flux.just("失败\n");
                            }
                        })
        );
    }

    /**
     * 执行搜索 - 基于生成的搜索查询
     */
    private Flux<String> executeSearch(ContextData context, ReportStructureEntity paragraph) {
        String searchQuery = paragraph.getSearchQuery();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return Flux.just("  ❌ 搜索查询为空\n");
        }

        TavilySearchRequest request = new TavilySearchRequest();
        request.setQuery(searchQuery);
        request.setMax_results("3");
        request.setInclude_raw_content(false);

        return Flux.concat(
                Flux.just("  🌐 执行搜索... "),
                tavilySearchClient.search(request)
                        .map(response -> {
                            // 提取搜索结果
                            List<String> searchResults = new ArrayList<>();
                            StringBuilder detailedResults = new StringBuilder();

                            if (response.getResults() != null) {
                                detailedResults.append("SEARCH_RESULTS_START\n");
                                detailedResults.append(String.format("查询: %s\n", searchQuery));

                                for (TavilySearchResponse.SearchResult result : response.getResults()) {
                                    searchResults.add(result.getTitle() + ": " + result.getContent());

                                    // 输出详细搜索结果信息
                                    detailedResults.append(String.format("RESULT_ITEM_START\n"));
                                    detailedResults.append(String.format("标题: %s\n", result.getTitle()));
                                    detailedResults.append(String.format("内容: %s\n", result.getContent()));
                                    detailedResults.append(String.format("分数: %.2f\n", result.getScore() != null ? result.getScore() : 0.0));
                                    detailedResults.append(String.format("链接: %s\n", result.getUrl()));
                                    detailedResults.append("RESULT_ITEM_END\n");
                                }
                                detailedResults.append("SEARCH_RESULTS_END\n");
                            }

                            // 同步更新段落状态
                            paragraph.setSearchResults(searchResults);

                            return detailedResults.toString() + String.format("找到 %d 条结果\n", searchResults.size());
                        })
                        .onErrorReturn("搜索失败\n")
        );
    }

    /**
     * 总结搜索结果 - 基于搜索结果生成段落内容
     */
    private Flux<String> summarizeSearchResults(ContextData context, ReportStructureEntity paragraph) {
        List<String> searchResults = paragraph.getSearchResults();
        if (searchResults == null || searchResults.isEmpty()) {
            return Flux.just("  ⚠️ 没有搜索结果可总结\n");
        }

        // 构建输入JSON
        String searchResultsJson = JSONUtil.toJsonStr(searchResults);
        String inputJson = String.format("""
                        {
                            "title": "%s",
                            "content": "%s",
                            "search_query": "%s",
                            "search_results": %s
                        }
                        """,
                paragraph.getTitle(),
                paragraph.getContent(),
                paragraph.getSearchQuery(),
                searchResultsJson);

        return Flux.concat(
                Flux.just("  📝 总结搜索结果... "),
                firstSummaryNode.execute(inputJson, context)
                        .collectList() // 等待LLM完成
                        .flatMapMany(chunks -> {
                            String result = String.join("", chunks);
                            try {
                                JsonNode summaryJson = objectMapper.readTree(result);
                                String paragraphState = summaryJson.get("paragraph_latest_state").asText();

                                // 同步更新段落状态
                                paragraph.setLatestState(paragraphState);
                                log.info("搜索总结内容:{}", paragraphState);
                                return Flux.just("完成\n");
                            } catch (Exception e) {
                                log.error("解析搜索总结失败: {}", result, e);
                                return Flux.just("失败\n");
                            }
                        })
        );
    }

    /**
     * 丰富段落内容 - 基于反思进一步完善
     */
    private Flux<String> enrichParagraph(ContextData context, ReportStructureEntity paragraph) {
        // 构建反思输入JSON
        String reflectionInputJson = String.format("""
                        {
                            "title": "%s",
                            "content": "%s",
                            "paragraph_latest_state": "%s"
                        }
                        """,
                paragraph.getTitle(),
                paragraph.getContent(),
                paragraph.getLatestState() != null ? paragraph.getLatestState() : "");

        return Flux.concat(
                // 1. 反思分析，生成新的搜索查询
                Flux.just("  🤔 反思分析... "),
                reflectionNode.execute(reflectionInputJson, context)
                        .collectList() // 等待LLM完成
                        .flatMapMany(chunks -> {
                            String result = String.join("", chunks);
                            try {
                                JsonNode reflectionJson = objectMapper.readTree(result);
                                String reflectionQuery = reflectionJson.get("search_query").asText();
                                String reflectionReasoning = reflectionJson.get("reasoning").asText();

                                // 保存反思结果
                                paragraph.setReflectionQuery(reflectionQuery);
                                paragraph.setReflectionReasoning(reflectionReasoning);
                                log.info("反思分析请求:{}", reflectionInputJson);
                                log.info("反思分析结果:{}", reflectionJson);
                                return Flux.just("完成\n");
                            } catch (Exception e) {
                                log.error("解析反思结果失败: {}", result, e);
                                return Flux.just("失败\n");
                            }
                        }),

                // 2. 基于反思查询执行搜索
                Flux.defer(() -> executeReflectionSearch(context, paragraph)),

                // 3. 基于反思搜索结果丰富内容
                Flux.defer(() -> finalizeEnrichment(context, paragraph))
        );
    }

    /**
     * 执行反思搜索
     */
    private Flux<String> executeReflectionSearch(ContextData context, ReportStructureEntity paragraph) {
        String reflectionQuery = paragraph.getReflectionQuery();
        if (reflectionQuery == null || reflectionQuery.trim().isEmpty()) {
            return Flux.just("  ⚠️ 反思查询为空，跳过搜索\n");
        }

        TavilySearchRequest request = new TavilySearchRequest();
        request.setQuery(reflectionQuery);
        request.setMax_results("3");
        request.setInclude_raw_content(false);

        return Flux.concat(
                Flux.just("  🔍 执行反思搜索... "),
                tavilySearchClient.search(request)
                        .map(response -> {
                            // 提取搜索结果
                            List<String> reflectionSearchResults = new ArrayList<>();
                            StringBuilder detailedResults = new StringBuilder();

                            if (response.getResults() != null) {
                                detailedResults.append("REFLECTION_SEARCH_RESULTS_START\n");
                                detailedResults.append(String.format("反思查询: %s\n", reflectionQuery));

                                for (TavilySearchResponse.SearchResult result : response.getResults()) {
                                    reflectionSearchResults.add(result.getTitle() + ": " + result.getContent());

                                    // 输出详细搜索结果信息
                                    detailedResults.append(String.format("RESULT_ITEM_START\n"));
                                    detailedResults.append(String.format("标题: %s\n", result.getTitle()));
                                    detailedResults.append(String.format("内容: %s\n", result.getContent()));
                                    detailedResults.append(String.format("分数: %.2f\n", result.getScore() != null ? result.getScore() : 0.0));
                                    detailedResults.append(String.format("链接: %s\n", result.getUrl()));
                                    detailedResults.append("RESULT_ITEM_END\n");
                                }
                                detailedResults.append("REFLECTION_SEARCH_RESULTS_END\n");
                            }

                            // 同步更新段落状态
                            paragraph.setReflectionSearchResults(reflectionSearchResults);

                            return detailedResults.toString() + String.format("找到 %d 条补充结果\n", reflectionSearchResults.size());
                        })
                        .onErrorReturn("反思搜索失败\n")
        );
    }

    /**
     * 最终丰富内容
     */
    private Flux<String> finalizeEnrichment(ContextData context, ReportStructureEntity paragraph) {
        // 构建最终输入JSON
        String originalSearchResults = JSONUtil.toJsonStr(paragraph.getSearchResults());
        String reflectionSearchResults = JSONUtil.toJsonStr(paragraph.getReflectionSearchResults());

        String inputJson = String.format("""
                        {
                            "title": "%s",
                            "content": "%s",
                            "search_query": "%s",
                            "search_results": %s,
                            "paragraph_latest_state": "%s"
                        }
                        """,
                paragraph.getTitle(),
                paragraph.getContent(),
                paragraph.getReflectionQuery() != null ? paragraph.getReflectionQuery() : "",
                reflectionSearchResults != null ? reflectionSearchResults : "[]",
                paragraph.getLatestState() != null ? paragraph.getLatestState() : "");

        return Flux.concat(
                Flux.just("  ✨ 丰富内容... "),
                reflectionSummaryNode.execute(inputJson, context)
                        .collectList() // 等待LLM完成
                        .flatMapMany(chunks -> {
                            String result = String.join("", chunks);
                            try {
                                JsonNode updatedJson = objectMapper.readTree(result);
                                String updatedContent = updatedJson.get("updated_paragraph_latest_state").asText();

                                // 同步更新段落最终状态
                                paragraph.setLatestState(updatedContent);

                                return Flux.just("完成\n");
                            } catch (Exception e) {
                                log.error("解析丰富内容失败: {}", result, e);
                                return Flux.just("失败\n");
                            }
                        })
        );
    }

    /**
     * 生成最终报告 - 基于所有段落的最终状态
     */
    private Flux<String> generateFinalReport(ContextData context) {
        List<ReportStructureEntity> paragraphs = context.getParagraphs();

        if (paragraphs == null || paragraphs.isEmpty()) {
            return Flux.just("❌ 没有段落信息可用于生成报告\n");
        }

        // 验证所有段落都有最终状态
        boolean allParagraphsReady = paragraphs.stream()
                .allMatch(p -> p.getLatestState() != null && !p.getLatestState().trim().isEmpty());

        if (!allParagraphsReady) {
            return Flux.just("⚠️ 部分段落未完成处理，跳过报告生成\n");
        }

        // 构建段落数组JSON
        String paragraphsJson = JSONUtil.toJsonStr(paragraphs.stream()
                .map(p -> Map.of(
                        "title", p.getTitle(),
                        "paragraph_latest_state", p.getLatestState() != null ? p.getLatestState() : ""
                ))
                .toList());

        return Flux.concat(
                Flux.just("📄 正在生成最终报告...\n\n"),
                Flux.just("FINAL_REPORT_START\n"),
                reportFormattingNode.execute(paragraphsJson, context)
                        .collectList() // 等待报告生成完成
                        .flatMapMany(chunks -> {
                            log.info("生成的最终报告请求参数:{}", paragraphsJson);
                            String finalReport = String.join("", chunks);
                            log.info("生成的最终报告内容: {}", finalReport);
                            // 保存最终报告
                            context.setFinalReport(finalReport);
                            return Flux.just(finalReport);
                        }),
                Flux.just("\nFINAL_REPORT_END\n"),
                Flux.just("\n🎉 深度研究完成！")
        );
    }

    public String researchSync(String query) {
        ContextData contextData = new ContextData();
        contextData.setQuery(query);
        log.info("开始处理段落,用户搜索条件为:{}", query);
        ParameterizedTypeReference<List<ReportStructureEntity>> typeReference = new ParameterizedTypeReference<>() {
        };
        List<ReportStructureEntity> reportStructureEntities = reportStructureNode.callByEntity(query, typeReference);
        contextData.setParagraphs(reportStructureEntities);
        log.info("段落处理完毕 数量为:{}", reportStructureEntities.size());
        for (int i = 0; i < reportStructureEntities.size(); i++) {

            searchAndSummary(contextData, i);

            reflectionLoop(contextData, i);
        }

        report(contextData);
        return contextData.getFinalReport();
    }

    private void report(ContextData contextData) {
        List<Map<Object, Object>> inputList = new ArrayList<>();
        List<ReportStructureEntity> paragraphs = contextData.getParagraphs();
        for (ReportStructureEntity paragraph : paragraphs) {
            Map<Object, Object> map = MapUtil.builder()
                    .put("title", paragraph.getTitle())
                    .put("paragraph_latest_state", paragraph.getLatestState()).build();
            inputList.add(map);
        }
        log.info("处理最终报告 input:{}", JSONUtil.toJsonStr(inputList));
        String result = reportFormattingNode.call(JSONUtil.toJsonStr(inputList));
        log.info("最终报告结果:{}",result);
        contextData.setFinalReport(result);
    }

    private void reflectionLoop(ContextData contextData, int index) {
        int maxLength = 2;
        ReportStructureEntity reportStructureEntity = contextData.getParagraphs().get(index);
        for (int i = 0; i < maxLength; i++) {
            String reflectionInput = String.format("""
                            {
                                "title": "%s",
                                "content": "%s",
                                "paragraph_latest_state": "%s",
                            }
                            """,
                    reportStructureEntity.getTitle(),
                    reportStructureEntity.getContent(),
                    reportStructureEntity.getLatestState());
            log.info("开始反思 {}/{} input:{}", i + 1, maxLength, reflectionInput);
            ReflectionResData reflectionResData = reflectionNode.callByEntity(reflectionInput, new ParameterizedTypeReference<ReflectionResData>() {
            });
            log.info("反思结果:{}", JSONUtil.toJsonStr(reflectionResData));

            TavilySearchRequest request = new TavilySearchRequest();
            String searchQuery = reflectionResData.getSearch_query();
            request.setQuery(searchQuery);
            TavilySearchResponse searchResponse = tavilySearchClient.search(request).block();
            if (searchResponse == null) {
                log.error("搜索失败");
                continue;
            }

            // 添加搜索历史
            addSearchResult(searchResponse, searchQuery, reportStructureEntity);

            String reflectionSummaryInput = String.format("""
                            {
                                "title": "%s",
                                "content": "%s",
                                "search_query": "%s",
                                "search_results": "%s",
                                "paragraph_latest_state":"%s"
                            }
                            """,
                    reportStructureEntity.getTitle(),
                    reportStructureEntity.getContent(),
                    searchQuery,
                    JSONUtil.toJsonStr(searchResultForPrompt(searchResponse.getResults(), 2000)),
                    reportStructureEntity.getLatestState());
            log.info("反思总结 input:{}", reflectionSummaryInput);
            // 总结
            JSONObject summaryObj = reflectionSummaryNode.callByEntity(reflectionSummaryInput, new ParameterizedTypeReference<>() {
            });
            log.info("总结内容:{}", summaryObj);
            String updatedParagraphLatestState = summaryObj.getStr("updated_paragraph_latest_state");
            reportStructureEntity.setLatestState(updatedParagraphLatestState);
        }

    }

    private List<String> searchResultForPrompt(List<TavilySearchResponse.SearchResult> results, int maxLength) {
        // 截断
        List<String> truncatedResults = new ArrayList<>();
        for (TavilySearchResponse.SearchResult result : results) {
            String content = result.getContent();
            if (content.length() > maxLength) {
                truncatedResults.add(content.substring(0, maxLength));
            }
        }
        return truncatedResults;
    }

    private void addSearchResult(TavilySearchResponse searchResponse, String searchQuery, ReportStructureEntity reportStructure) {
        if (searchResponse == null) {
            return;
        }
        List<TavilySearchResponse.SearchResult> results = searchResponse.getResults();
        List<SearchResult> searchResultList = new ArrayList<>();
        for (TavilySearchResponse.SearchResult result : results) {
            SearchResult searchResult = new SearchResult();
            searchResult.setQuery(searchQuery);
            searchResult.setUrl(result.getUrl());
            searchResult.setTitle(result.getTitle());
            searchResult.setContent(result.getContent());
            searchResult.setScore(result.getScore());
            searchResultList.add(searchResult);
        }
        reportStructure.setSearchResultList(searchResultList);
    }

    private void searchAndSummary(ContextData contextData, int i) {

        ReportStructureEntity reportStructure = contextData.getParagraphs().get(i);
        String title = reportStructure.getTitle();
        String content = reportStructure.getContent();
        Map<Object, Object> inputMap = MapUtil.builder()
                .put("title", title)
                .put("content", content)
                .build();
        log.info("首次搜索内容:{}", JSONUtil.toJsonStr(inputMap));
        // llm获取搜索内容
        QueryData queryData = firstSearchNode.callByEntity(JSONUtil.toJsonStr(inputMap), new ParameterizedTypeReference<>() {
        });
        log.info("llm返回搜索条件:{}", JSONUtil.toJsonStr(queryData));

        // 执行网络搜索
        TavilySearchRequest request = new TavilySearchRequest();
        String searchQuery = queryData.getSearch_query();
        request.setQuery(searchQuery);
        TavilySearchResponse searchResponse = tavilySearchClient.search(request).block();
        if (searchResponse == null) {
            log.error("搜索失败");
            return;
        }
        addSearchResult(searchResponse, searchQuery, reportStructure);

        String firstSummaryInput = String.format("""
                        {
                            "title": "%s",
                            "content": "%s",
                            "search_query": "%s",
                            "search_results": "%s"
                        }
                        """,
                reportStructure.getTitle(),
                reportStructure.getContent(),
                searchQuery,
                JSONUtil.toJsonStr(searchResultForPrompt(searchResponse.getResults(), 2000)));

        log.info("首次总结input:{}", firstSummaryInput);
        // 总结
        JSONObject summaryObj = firstSummaryNode.callByEntity(firstSummaryInput, new ParameterizedTypeReference<>() {
        });
        log.info("llm返回总结结果:{}", summaryObj);
        String paragraphLatestState = summaryObj.getStr("paragraph_latest_state");
        reportStructure.setLatestState(paragraphLatestState);
    }
}
