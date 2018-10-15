package no.nav.saf.restconfig;


import no.nav.saf.fasit.ServiceuserAlias;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    public static final int TIMEOUT = 30_000;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
        return restTemplateBuilder
                .setReadTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
    }
}
