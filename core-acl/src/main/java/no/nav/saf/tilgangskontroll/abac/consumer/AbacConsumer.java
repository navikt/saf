package no.nav.saf.tilgangskontroll.abac.consumer;

import static java.util.Collections.singletonList;

import no.nav.saf.integration.fasit.ServiceuserAlias;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.exception.UnexpectedHttpCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class AbacConsumer {
	private static final MediaType APPLICATION_XACML_AND_JSON = MediaType.parseMediaType("application/xacml+json");

	private final RestTemplate restTemplate;
	private final String url;
	private final AbacRequestMapper abacRequestMapper;
	private final AbacResponseMapper abacResponseMapper;

	public AbacConsumer(RestTemplateBuilder restTemplateBuilder,
						@Value("${abac.pdp.endpoint.url}") String url,
						ServiceuserAlias serviceuserAlias,
						AbacRequestMapper abacRequestMapper,
						AbacResponseMapper abacResponseMapper) {
		this.url = url;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(10))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
		this.abacRequestMapper = abacRequestMapper;
		this.abacResponseMapper = abacResponseMapper;
	}

	public XacmlResponse evaluate(XacmlRequest requestBody) {
		HttpEntity<String> httpRequest = prepareHttpRequest(requestBody);

		ResponseEntity<String> abacResult = restTemplate.postForEntity(url, httpRequest, String.class);

		if (!abacResult.getStatusCode().is2xxSuccessful()) {
			throw new UnexpectedHttpCodeException(abacResult.getStatusCodeValue(), 200, abacResult.getStatusCode()
					.getReasonPhrase());
		}

		return abacResponseMapper.map(abacResult.getBody());
	}

	private HttpEntity<String> prepareHttpRequest(XacmlRequest request) {
		String requestAsJson = abacRequestMapper.map(request);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_XACML_AND_JSON);
		headers.setAccept(singletonList(APPLICATION_XACML_AND_JSON));
		return new HttpEntity<>(requestAsJson, headers);
	}
}