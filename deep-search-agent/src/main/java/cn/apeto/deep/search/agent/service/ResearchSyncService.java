package cn.apeto.deep.search.agent.service;

import cn.apeto.deep.search.agent.client.TavilySearchClient;
import cn.apeto.deep.search.agent.client.request.TavilySearchRequest;
import cn.apeto.deep.search.agent.client.response.TavilySearchResponse;
import cn.apeto.deep.search.agent.common.FormatUtils;
import cn.apeto.deep.search.agent.domain.*;
import cn.apeto.deep.search.agent.nodes.*;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author apeto
 * @create 2025/12/25 09:47
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchSyncService {

    private final ReportStructureNode reportStructureNode;
    private final FirstSearchNode firstSearchNode;
    private final FirstSummaryNode firstSummaryNode;
    private final ReflectionNode reflectionNode;
    private final ReflectionSummaryNode reflectionSummaryNode;
    private final ReportFormattingNode reportFormattingNode;
    private final TavilySearchClient tavilySearchClient;

    public String research(String query) {
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
                                "paragraph_latest_state": "%s"
                            }
                            """,
                    reportStructureEntity.getTitle(),
                    reportStructureEntity.getContent(),
                    reportStructureEntity.getLatestState());
            log.info("开始反思 {}/{} input:{}", i + 1, maxLength, reflectionInput);
            ReflectionResData reflectionResData = reflectionNode.callByEntity(reflectionInput, new ParameterizedTypeReference<>() {
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
                    JSONUtil.toJsonStr(FormatUtils.searchResultForPrompt(searchResponse.getResults(), 2000)),
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



    public static void addSearchResult(TavilySearchResponse searchResponse, String searchQuery, ReportStructureEntity reportStructure) {
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
                JSONUtil.toJsonStr(FormatUtils.searchResultForPrompt(searchResponse.getResults(), 2000)));

        log.info("首次总结input:{}", firstSummaryInput);
        // 总结
        JSONObject summaryObj = firstSummaryNode.callByEntity(firstSummaryInput, new ParameterizedTypeReference<>() {
        });
        log.info("llm返回总结结果:{}", summaryObj);
        String paragraphLatestState = summaryObj.getStr("paragraph_latest_state");
        reportStructure.setLatestState(paragraphLatestState);
    }
}
