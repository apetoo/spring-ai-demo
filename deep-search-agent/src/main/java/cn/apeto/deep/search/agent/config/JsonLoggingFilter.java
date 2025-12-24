package cn.apeto.deep.search.agent.config;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JSON格式日志过滤器 - 适合ELK日志收集
 */
@Slf4j
public class JsonLoggingFilter implements ExchangeFilterFunction {

    private final String serviceName;

    public JsonLoggingFilter(String serviceName) {
        this.serviceName = serviceName;
    }

    @Data
    public static class HttpLog {
        private String timestamp;
        private String level = "INFO";
        private String service;
        private String traceId;
        private String requestId;
        private String type = "HTTP_CLIENT";
        private String method;
        private String url;
        private String path;
        private Integer status;
        private Long durationMs;
        private String durationHuman;
        private String result;
        private Map<String, String> metadata = new HashMap<>();

        public String toJson() {
            return JSONUtil.toJsonStr(this);
        }
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Instant startTime = Instant.now();
        String traceId = getTraceId();
        String requestId = UUID.randomUUID().toString();

        // 创建日志对象
        HttpLog httpLog = new HttpLog();
        httpLog.setTimestamp(java.time.format.DateTimeFormatter.ISO_INSTANT.format(startTime));
        httpLog.setService(serviceName);
        httpLog.setTraceId(traceId);
        httpLog.setRequestId(requestId);
        httpLog.setMethod(request.method().name());
        httpLog.setUrl(request.url().toString());
        httpLog.setPath(extractPath(request.url().toString()));

        // 添加请求头信息
        request.headers().forEach((key, values) -> {
            if (!key.toLowerCase().contains("authorization") &&
                !key.toLowerCase().contains("password")) {
                httpLog.getMetadata().put("req.header." + key, String.join(",", values));
            }
        });

        // 执行请求
        return next.exchange(request)
            .doOnSuccess(response -> {
                Duration duration = Duration.between(startTime, Instant.now());
                completeLog(httpLog, response, duration, true);
            })
            .doOnError(error -> {
                Duration duration = Duration.between(startTime, Instant.now());
                httpLog.setStatus(0);
                httpLog.setResult("ERROR");
                httpLog.setLevel("ERROR");
                httpLog.getMetadata().put("error", error.getClass().getSimpleName());
                httpLog.getMetadata().put("error.message", error.getMessage());
                completeLog(httpLog, null, duration, false);
            });
    }

    private void completeLog(HttpLog httpLog, ClientResponse response,
                            Duration duration, boolean success) {
        if (response != null) {
            httpLog.setStatus(response.statusCode().value());
            httpLog.setResult(response.statusCode().is2xxSuccessful() ? "SUCCESS" : "FAILURE");

            // 记录响应头
            response.headers().asHttpHeaders().forEach((key, values) -> {
                if (!key.toLowerCase().contains("authorization")) {
                    httpLog.getMetadata().put("res.header." + key, String.join(",", values));
                }
            });
        }

        httpLog.setDurationMs(duration.toMillis());
        httpLog.setDurationHuman(formatDuration(duration));

        // 设置日志级别
        if (httpLog.getStatus() != null && httpLog.getStatus() >= 400) {
            httpLog.setLevel("WARN");
        }
        if (duration.toMillis() > 1000) {
            httpLog.setLevel("WARN");
            httpLog.getMetadata().put("performance", "SLOW");
        }

        // 输出JSON日志
        log.info(httpLog.toJson());

        // 如果失败或慢请求，也输出到控制台
        if (!success || duration.toMillis() > 1000) {
            String consoleLog = String.format("[%s][%s] HTTP %s %s - %d %dms %s",
                httpLog.getTraceId(), httpLog.getRequestId(),
                httpLog.getMethod(), httpLog.getPath(),
                httpLog.getStatus() != null ? httpLog.getStatus() : 0,
                httpLog.getDurationMs(),
                httpLog.getResult());

            if (!success) {
                log.error(consoleLog);
            } else {
                log.warn(consoleLog);
            }
        }
    }

    private String extractPath(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getPath() != null ? uri.getPath() : "/";
        } catch (Exception e) {
            return url;
        }
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String formatDuration(Duration duration) {
        long ms = duration.toMillis();
        if (ms < 1000) {
            return ms + "ms";
        } else if (ms < 60000) {
            return (ms / 1000) + "s";
        } else {
            return (ms / 60000) + "m";
        }
    }
}
