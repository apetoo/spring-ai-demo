package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 反思节点 - 批判性思考
 */
@Component
public class ReflectionNode extends BaseNode {

    protected ReflectionNode(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。你负责为研究报告构建全面的段落。你将获得段落标题、计划内容摘要，以及你已经创建的段落最新状态，所有这些都将按照以下JSON模式定义提供：
                
                <INPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "title": {"type": "string"},
                        "content": {"type": "string"},
                        "paragraph_latest_state": {"type": "string"}
                    }
                }
                </INPUT JSON SCHEMA>
                
                你可以使用一个网络搜索工具，该工具接受'search_query'作为参数。
                你的任务是反思段落文本的当前状态，思考是否遗漏了主题的某些关键方面，并提供最佳的网络搜索查询来丰富最新状态。
                请按照以下JSON模式定义格式化输出：
                
                <OUTPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "search_query": {"type": "string"},
                        "reasoning": {"type": "string"}
                    }
                }
                </OUTPUT JSON SCHEMA>
                
                确保输出是一个符合上述输出JSON模式定义的JSON对象。
                只返回JSON对象，不要有解释或额外文本。
                """;
    }

    @Override
    public Flux<String> execute(String input, ContextData context) {
        // input 参数包含了段落信息和当前状态的JSON
        return callLLM(input);
    }
}
