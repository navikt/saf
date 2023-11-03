package no.nav.saf.anticorruptionlayer.nav;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import static no.nav.saf.anticorruptionlayer.nav.NavHrBedriftResponse.nei;
import static no.nav.saf.cache.LokalCacheConfig.HR_NAV_BEDRIFT_CACHE;

@Slf4j
@Component
public class NavHrBedriftConsumer {

	private final WebClient webClient;

	public NavHrBedriftConsumer(WebClient webClient,
								ObjectMapper objectMapper,
								SafProperties safProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getHrNavBedrift())
				.exchangeStrategies(ExchangeStrategies.builder().codecs(clientCodecConfigurer ->
								clientCodecConfigurer.customCodecs()
										.register(new Jackson2JsonDecoder(objectMapper, MimeTypeUtils.APPLICATION_JSON)))
						.build())
				.build();
	}

	@Cacheable(HR_NAV_BEDRIFT_CACHE)
	public NavHrBedriftResponse getNavBedrift(String organisasjonsnummer) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/json/Hr/Nav_Bedrift/HR_NAV_BEDRIFT")
						.queryParam("BEDRIFTNR_INN", organisasjonsnummer)
						.build())
				.exchangeToMono(clientResponse -> {
					if (clientResponse.statusCode().is2xxSuccessful()) {
						return clientResponse.bodyToMono(NavHrBedriftResponse.class);
					} else {
						return clientResponse.createError();
					}
				})
				.onErrorResume(WebClientException.class, e -> {
					log.error("Kall til Hr/NAV_Bedrift feilet message={}. Returnerer at organisasjonsnummer={} ikke er NAV bedrift",
							e.getMessage(), organisasjonsnummer, e);
					return Mono.empty();
				})
				.defaultIfEmpty(nei(organisasjonsnummer))
				.block();
	}
}
