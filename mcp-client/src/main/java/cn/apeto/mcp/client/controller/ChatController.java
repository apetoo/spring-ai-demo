package cn.apeto.mcp.client.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author apeto
 * @create 2025/5/20 18:44
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;


    @GetMapping(value = "/generate", produces = "text/event-stream")
    public Flux<String> generate(@RequestParam String message) {

        Message userMessage = new UserMessage(message);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Message systemMessage = systemPromptTemplate.createMessage();
        SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder().build();
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return chatClient.prompt(new Prompt(List.of(userMessage, systemMessage)))
                .advisors(chatMemoryAdvisor, loggerAdvisor)
                .stream()
                .chatResponse()
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                .doOnComplete(() -> {
                    log.info("请求结束");
                });
    }


    @PostMapping("/test")
    public String test() {
        log.info("测试日志");
        return "test";
    }

}
