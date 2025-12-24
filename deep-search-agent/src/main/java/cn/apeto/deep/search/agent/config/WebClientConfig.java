package cn.apeto.deep.search.agent.config;

import cn.apeto.deep.search.agent.client.TavilySearchClient;
import cn.apeto.deep.search.agent.config.properties.ThirdApiConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("spring.application.name")
    private String name;

    @Bean("tavilySearchWebClient")
    public WebClient tavilySearchWebClient(WebClient.Builder builder, ThirdApiConfig thirdApiConfig) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(Duration.ofSeconds(20))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(15))
                        .addHandlerLast(new WriteTimeoutHandler(15))
                )
                .compress(true);

        ThirdApiConfig.Config tavilySearchConfig = thirdApiConfig.getTavilySearch();
        return builder
                .baseUrl(tavilySearchConfig.getBaseUrl())
                .filter(new JsonLoggingFilter(name))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> headers.setBearerAuth(tavilySearchConfig.getApiKey()))
                .build();
    }

    @Bean
    public TavilySearchClient githubApiClient(@Qualifier("tavilySearchWebClient") WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();
        return factory.createClient(TavilySearchClient.class);
    }
}
