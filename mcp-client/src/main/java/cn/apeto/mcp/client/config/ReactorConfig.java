package cn.apeto.mcp.client.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Hooks;

@Configuration
public class ReactorConfig implements CommandLineRunner {

    @Override
    public void run(String... args) {
        Hooks.enableAutomaticContextPropagation();
    }
}
