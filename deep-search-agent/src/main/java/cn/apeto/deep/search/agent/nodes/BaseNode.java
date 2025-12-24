package cn.apeto.deep.search.agent.nodes;

import cn.apeto.deep.search.agent.domain.ContextData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author apeto
 * @create 2025/12/9 13:49
 */
public abstract class BaseNode {

    protected final ChatClient chatClient;

    protected BaseNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    /**
     * 获取系统提示词
     */
    public abstract String getSystemPrompt();

    /**
     * 执行节点逻辑，返回流式输出
     */
    public abstract Flux<String> execute(String input, ContextData context);


    /**
     * 调用LLM流式输出
     */
    protected Flux<String> callLLM(String prompt) {

        Message userMessage = new UserMessage(prompt);
        Message systemMessage = new SystemMessage(getSystemPrompt());
        SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder().build();
        return chatClient.prompt(new Prompt(List.of(userMessage, systemMessage)))
                .advisors(loggerAdvisor)
                .stream()
                .content();
    }

    public  <T> T callByEntity(String prompt, ParameterizedTypeReference<T> entityClass) {
        Message userMessage = new UserMessage(prompt);
        Message systemMessage = new SystemMessage(getSystemPrompt());
        SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder().build();
        return chatClient.prompt(new Prompt(List.of(userMessage, systemMessage)))
                .advisors(loggerAdvisor)
                .call().entity(entityClass);
    }

    public String call(String prompt) {
        Message userMessage = new UserMessage(prompt);
        Message systemMessage = new SystemMessage(getSystemPrompt());
        SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder().build();
        return chatClient.prompt(new Prompt(List.of(userMessage, systemMessage)))
                .advisors(loggerAdvisor)
                .call().content();
    }
}
