package edu.cit.amihan.medibook.fda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FdaConfig {

    @Bean
    public WebClient fdaWebClient() {
        ObjectMapper mapper = new ObjectMapper();
        return WebClient.builder()
                .baseUrl("https://api.fda.gov")
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }
}