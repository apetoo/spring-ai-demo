package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author apeto
 * @create 2025/12/9 17:35
 */
@Component
public class ReportStructureNode extends BaseNode {

    protected ReportStructureNode(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。给定一个查询，你需要规划一个报告的结构和其中包含的段落。最多三个段落。
                确保段落的排序合理有序。
                一旦大纲创建完成，你将获得工具来分别为每个部分搜索网络并进行反思。
                请按照以下JSON模式定义格式化输出：
                
                <OUTPUT JSON SCHEMA>
                {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "title": {"type": "string"},
                            "content": {"type": "string"}
                        }
                    }
                }
                </OUTPUT JSON SCHEMA>
                
                标题和内容属性将用于更深入的研究。
                确保输出是一个符合上述输出JSON模式定义的JSON对象。
                只返回JSON对象，不要有解释或额外文本。内容必须是中文
                """;
    }

    @Override
    public Flux<String> execute(String input, ContextData context) {

        StringBuilder result = new StringBuilder();

        return callLLM(input)
                .doOnNext(result::append)
                .doOnComplete(() -> context.setReportStructure(result.toString()));
    }
}



