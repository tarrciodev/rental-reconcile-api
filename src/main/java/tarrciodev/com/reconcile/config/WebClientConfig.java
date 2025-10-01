package tarrciodev.com.reconcile.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // timeout de conexão
                .responseTimeout(Duration.ofMinutes(10)); // timeout de resposta (aumentei p/ 10 min)

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
