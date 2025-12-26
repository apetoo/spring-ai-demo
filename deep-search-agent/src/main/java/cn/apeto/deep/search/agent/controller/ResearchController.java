package cn.apeto.deep.search.agent.controller;

import cn.apeto.deep.search.agent.service.ResearchService;
import cn.apeto.deep.search.agent.service.ResearchSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author apeto
 * @create 2025/12/9 19:05
 */
@RequestMapping("/api/research")
@RestController
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchService researchService;
    private final ResearchSyncService researchSyncService;

    /**
     * 流式返回研究过程
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> research(@RequestParam String query) {
        return researchService.research(query);
    }

    @GetMapping("/sync")
    public String researchSync(@RequestParam String query) {
        return researchSyncService.research(query);
    }
}
