package cn.apeto.deep.search.agent.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author apeto
 * @create 2025/12/2 16:43
 */
@Data
@Component
@ConfigurationProperties(prefix = "apeto.third-api")
public class ThirdApiConfig {

    private Config tavilySearch;


    @Data
    public static class Config {
        private String baseUrl;
        private String apiKey;
    }
}
