package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 报告格式化节点 - 生成最终报告
 */
@Component
public class ReportFormattingNode extends BaseNode {

    protected ReportFormattingNode(ChatClient chatClientText) {
        super(chatClientText);
    }


    @Override
    public String getSystemPrompt() {
        return """
                你是一位深度研究助手。你已经完成了研究并构建了报告中所有段落的最终版本。
                你将获得以下JSON格式的数据：
                
                <INPUT JSON SCHEMA>
                {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "title": {"type": "string"},
                            "paragraph_latest_state": {"type": "string"}
                        }
                    }
                }
                </INPUT JSON SCHEMA>
                
                你的任务是将报告格式化为美观的Markdown格式报告。
                
                要求：
                1. 直接输出Markdown格式的报告内容，不要包含任何JSON
                2. 使用段落标题创建二级标题（## 标题）
                3. 每个段落的内容使用paragraph_latest_state字段
                4. 在报告开头添加一个主标题
                5. 如果没有结论段落，在末尾添加一个结论部分
                6. 确保输出是纯Markdown格式，可以直接渲染
                
                示例输出格式：
                # 研究报告标题
                
                ## 段落1标题
                段落1的详细内容...
                
                ## 段落2标题
                段落2的详细内容...
                
                ## 结论
                基于以上分析的结论...
                """;
    }

    @Override
    public Flux<String> execute(String input, ContextData context) {
        return callLLM(input);
    }
}
