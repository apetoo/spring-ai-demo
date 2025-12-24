package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 第一次总结节点
 */
@Slf4j
@Component
public class FirstSummaryNode extends BaseNode {

    protected FirstSummaryNode(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。你将获得搜索查询、搜索结果以及你正在研究的报告段落，数据将按照以下JSON模式定义提供：
                
                <INPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "title": {"type": "string"},
                        "content": {"type": "string"},
                        "search_query": {"type": "string"},
                        "search_results": {
                            "type": "array",
                            "items": {"type": "string"}
                        }
                    }
                }
                </INPUT JSON SCHEMA>
                
                你的任务是作为研究者，使用搜索结果撰写与段落主题一致的内容，并适当地组织结构以便纳入报告中。
                请按照以下JSON模式定义格式化输出：
                
                <OUTPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "paragraph_latest_state": {"type": "string"}
                    }
                }
                </OUTPUT JSON SCHEMA>
                
                确保输出是一个符合上述输出JSON模式定义的JSON对象。
                只返回JSON对象，不要有解释或额外文本。
                """;
    }

    @Override
    public Flux<String> execute(String input, ContextData context) {
        // input 参数包含了段落信息和搜索结果的JSON
        return callLLM(input);
    }
}
