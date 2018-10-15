package no.nav.saf.context.gsak.hentGsakSaker;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.context.gsak.domain.GsakSakerTo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
class GsakConsumer {

    private final RestTemplate restTemplate;

    private final String gsakApiUrl;

    private final String xCorrelationId;

    public GsakConsumer(RestTemplate restTemplate,
                        @Value("${sak_saker_url}") String gsakApiUrl) {
        this.restTemplate = restTemplate;
        this.gsakApiUrl = gsakApiUrl;
        this.xCorrelationId = "1234567";
    }


    public GsakSakerTo getGsakSaker(final Integer saksId) {
        try {
            return restTemplate.getForObject(this.gsakApiUrl + "/" + Integer.toString(saksId), GsakSakerTo.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(String.format("getGsakSaker feilet med statusKode=%s. Fant ingen sak med saksId=%s.", e
                    .getStatusCode(), Integer.toString(saksId)));
        } catch (HttpServerErrorException e) {
            throw new RuntimeException(String.format("getGsakSaker feilet teknisk med statusKode=%s. Fant ingen sak med saksId=%s.", e
                    .getStatusCode(), Integer.toString(saksId)));
        }
    }

    protected HttpEntity createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new HttpEntity(headers);
    }
}