package no.nav.saf.integration.penrest;

import no.nav.saf.config.SafProperties;
import no.nav.saf.integration.azure.TokenConsumer;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Component
public class PensjonSakRest {
	private final RestTemplate restTemplate;
	private final TokenConsumer tokenConsumer;
	private final String pensjonSakScope;
	private final URI pensjonSakSammendragURI;

	PensjonSakRest (RestTemplateBuilder restTemplateBuilder,
					TokenConsumer tokenConsumer,
					SafProperties safProperties,
					HttpClientConnectionManager httpClientConnectionManager) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(createHttpClient(safProperties, httpClientConnectionManager)))
				.build();
		this.tokenConsumer = tokenConsumer;
		this.pensjonSakScope = safProperties.getEndpoints().getPenScope();
		this.pensjonSakSammendragURI = URI.create(safProperties.getEndpoints().getPen() + "/sammendrag");
	}
	public List<SakSammendrag> hentSakSammendragListe(String personident) throws HentSakSammendragListeSakManglerEierenhet, HentSakSammendragListePersonIkkeFunnet {
		return restTemplate.exchange(createRequestEntity(personident), new ParameterizedTypeReference<List<SakSammendrag>>() {}).getBody();
	}

	private RequestEntity<?> createRequestEntity(String personident) {
		return RequestEntity.get(pensjonSakSammendragURI)
				.header("fnr", personident)
				.header("Authorization", "bearer " + tokenConsumer.getClientCredentialToken(pensjonSakScope).getAccess_token())
				.accept(MediaType.APPLICATION_JSON)
				.build();
	}

	private CloseableHttpClient createHttpClient(SafProperties safProperties,
												 HttpClientConnectionManager httpClientConnectionManager) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(httpClientConnectionManager);

		safProperties.getProxy()
				.map(proxy -> new HttpHost(proxy.getHost(), proxy.getPort()))
				.ifPresent(proxyHost -> httpClientBuilder
						.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost))
				);

		return httpClientBuilder.build();
	}
}
