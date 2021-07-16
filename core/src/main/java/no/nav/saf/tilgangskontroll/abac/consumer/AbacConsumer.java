package no.nav.saf.tilgangskontroll.abac.consumer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.exceptions.AbacException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.util.Collections.singletonList;

@Slf4j
@Component
public class AbacConsumer {
    private static final String ABAC_INSTANCE = "abac";
    private static final MediaType APPLICATION_XACML_AND_JSON = MediaType.parseMediaType("application/xacml+json");

    private final RestTemplate restTemplate;
    private final String url;
    private final AbacRequestMapper abacRequestMapper;
    private final AbacResponseMapper abacResponseMapper;

    public AbacConsumer(RestTemplateBuilder restTemplateBuilder,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        @Value("${abac.pdp.endpoint.url}") String url,
                        ServiceuserAlias serviceuserAlias,
                        AbacRequestMapper abacRequestMapper,
                        AbacResponseMapper abacResponseMapper) {
        this.url = url;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> clientHttpRequestFactory)
                .setReadTimeout(Duration.ofSeconds(10))
                .setConnectTimeout(Duration.ofSeconds(5))
                .basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
        this.abacRequestMapper = abacRequestMapper;
        this.abacResponseMapper = abacResponseMapper;
    }

    @Monitor(value = "dok_consumer", extraTags = {"process", "abacEvaluate"}, histogram = true)
    @CircuitBreaker(name = ABAC_INSTANCE, fallbackMethod = "fallback")
    @Retry(name = ABAC_INSTANCE)
    public XacmlResponse evaluate(XacmlRequest requestBody) {
        try {
            HttpEntity<String> httpRequest = prepareHttpRequest(requestBody);
            ResponseEntity<String> abacResult = restTemplate.postForEntity(url, httpRequest, String.class);
            return abacResponseMapper.map(abacResult.getBody());
        } catch (HttpStatusCodeException e) {
            throw new AbacException(String.format("Kunne ikke evaluere tilgang for saksbehandler. Kall mot abac feilet teknisk med statusKode=%s. Feilmelding=%s",
                    e.getStatusCode(), e.getMessage()), e);
        }
    }

    private HttpEntity<String> prepareHttpRequest(XacmlRequest request) {
        String requestAsJson = abacRequestMapper.map(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_XACML_AND_JSON);
        headers.setAccept(singletonList(APPLICATION_XACML_AND_JSON));
        return new HttpEntity<>(requestAsJson, headers);
    }

    // Brukt av circuitbreaker
    public XacmlResponse fallback(XacmlRequest xacmlRequest, Throwable e) {
    	return XacmlResponse.denyWithInfo(e.getMessage());
	}
}