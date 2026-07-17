package edu.cit.amihan.medibook.fda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FdaConfig {

    @Bean
    public RestTemplate fdaRestTemplate() {
        return new RestTemplate();
    }
}
