package cn.apeto.deep.search.agent.client;

import cn.apeto.deep.search.agent.client.request.TavilySearchRequest;
import cn.apeto.deep.search.agent.client.response.TavilySearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * @author apeto
 * @create 2025/12/9 15:37
 */
@Component
@HttpExchange
public interface TavilySearchClient {

    @PostExchange("/search")
    Mono<TavilySearchResponse> search(@RequestBody TavilySearchRequest request);


}
