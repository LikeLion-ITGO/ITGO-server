package likelion.itgoserver.global.infra.ai.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiClientProperties.class)
public class AiClientConfig {

    @Bean
    RestClient aiRestClient(RestClient.Builder builder, AiClientProperties props) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(120));

        return builder
                .baseUrl(props.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}