package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 反思总结节点 - 基于反思搜索结果丰富段落
 */
@Component
public class ReflectionSummaryNode extends BaseNode {

    protected ReflectionSummaryNode(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。
                你将获得搜索查询、搜索结果、段落标题以及你正在研究的报告段落的预期内容。
                你正在迭代完善这个段落，并且段落的最新状态也会提供给你。
                数据将按照以下JSON模式定义提供：
                
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
                        },
                        "paragraph_latest_state": {"type": "string"}
                    }
                }
                </INPUT JSON SCHEMA>
                
                你的任务是根据搜索结果和预期内容丰富段落的当前最新状态。
                不要删除最新状态中的关键信息，尽量丰富它，只添加缺失的信息。
                适当地组织段落结构以便纳入报告中。
                请按照以下JSON模式定义格式化输出：
                
                <OUTPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "updated_paragraph_latest_state": {"type": "string"}
                    }
                }
                </OUTPUT JSON SCHEMA>
                
                确保输出是一个符合上述输出JSON模式定义的JSON对象。
                只返回JSON对象，不要有解释或额外文本。
                """;
    }

    @Override
    public Flux<String> execute(String input, ContextData context) {
        // input 参数包含了段落信息、搜索结果和当前状态的JSON
        return callLLM(input);
    }
}
