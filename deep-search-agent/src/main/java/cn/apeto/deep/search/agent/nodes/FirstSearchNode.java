package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author apeto
 * @create 2025/12/9
 * 第一次搜索节点 - 生成搜索关键词
 */
@Component
public class FirstSearchNode extends BaseNode {

    protected FirstSearchNode(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。你将获得报告中的一个段落，其标题和预期内容将按照以下JSON模式定义提供：
                
                <INPUT JSON SCHEMA>
                {
                    "type": "object",
                    "properties": {
                        "title": {"type": "string"},
                        "content": {"type": "string"}
                    }
                }
                </INPUT JSON SCHEMA>
                
                你可以使用一个网络搜索工具，该工具接受'search_query'作为参数。
                你的任务是思考这个主题，并提供最佳的网络搜索查询来丰富你当前的知识。
                请按照以下JSON模式定义格式化输出（文字请使用中文）：
                
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
        // input 参数包含了段落的JSON信息
        return callLLM(input);
    }
}
