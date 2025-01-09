package no.nav.saf.anticorruptionlayer.nav;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Slf4j
@Component
public class NavHrOrganisasjonConsumer {

	private final WebClient webClient;

	public NavHrOrganisasjonConsumer(WebClient webClient,
									 SafProperties safProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getHrNavUrl())
				.build();
	}

	NavHrOrganisasjonORDSResponse getAllNavOrganisasjon() {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/ords/dvh/dt_hr/nav_organisasjon_orgnummer")
						.build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(5));
				})
				.retrieve()
				.bodyToMono(NavHrOrganisasjonORDSResponse.class)
				.switchIfEmpty(Mono.error(new DecodingException("Tom respons fra endepunkt")))
				.doOnError(Throwable.class, NavHrOrganisasjonConsumer::logError)
				.retryWhen(Retry.backoff(100, Duration.ofSeconds(3)))
				.block();
	}

	private static void logError(Throwable e) {
		switch (e) {
			case DecodingException decodingException ->
					log.error("Klarte ikke dekode payload fra HR NAV Orgnummer tjenesten. Får ikke lastet cache. message={}",
							e.getMessage(), e);
			case WebClientException webClientException ->
					log.error("Kall til HR NAV Orgnummer tjenesten feilet. Får ikke lastet cache. message={}", e.getMessage(), e);
			case null, default ->
					log.error("Kall til HR NAV Orgnummer tjenesten feilet med en ukjent teknisk feil. message={}", e == null ? "null" : e.getMessage(), e);
		}
	}
}
