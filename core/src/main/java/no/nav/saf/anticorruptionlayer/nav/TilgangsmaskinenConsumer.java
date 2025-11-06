package no.nav.saf.anticorruptionlayer.nav;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.HabilitetReason;
import no.nav.saf.tilgangskontroll.pep.reasons.PersonUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;

import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;
import static org.springframework.boot.http.client.ClientHttpRequestFactorySettings.defaults;

@Slf4j
@Component
public class TilgangsmaskinenConsumer {

	private static final String TILGANGSMASKINEN_INSTANCE = "tilgangsmaskinen";

	public static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
	private final RestClient texasAuthorizedRestClient;
	private final SafProperties safProperties;

	public TilgangsmaskinenConsumer(RestClient texasAuthorizedRestClient, SafProperties safProperties) {
		ClientHttpRequestFactorySettings settings = defaults()
				.withConnectTimeout(Duration.ofSeconds(3))
				.withReadTimeout(READ_TIMEOUT);
		this.texasAuthorizedRestClient = texasAuthorizedRestClient.mutate()
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(settings))
				.baseUrl(safProperties.getEndpoints().getTilgangsmaskinen().getUrl())
				.build();
		this.safProperties = safProperties;
	}

	@Retry(name = TILGANGSMASKINEN_INSTANCE)
	@CircuitBreaker(name = TILGANGSMASKINEN_INSTANCE)
	public PepAnswer navIdentHasAccess(String identifikator, SafRequestContext safRequestContext) {
		try {
			if (safRequestContext.isUserIdNavAnsatt()) {
				return texasAuthorizedRestClient.post()
						.uri(uriBuilder -> uriBuilder.path("/api/v1/ccf/komplett/" + safRequestContext.getUserId()).build())
						.attributes(attributes ->
								attributes.put(TARGET_SCOPE, safProperties.getEndpoints().getTilgangsmaskinen().getScope()))
						.body(identifikator)
						.exchange((request, response) -> handleResponseFromTilgangsmaskinen(response));
			} else {
				log.error("Kunne ikke gjøre kall mot tilgangsmaskinen fordi userId ikke er en NAV-ident");
				throw new SafFunctionalException("Kunne ikke gjøre kall mot tilgangsmaskinen fordi brukers ident ikke er en NAV-ident");
			}
		} catch (Exception e) {
			log.error("Kall mot Tilgangsmaskinen feilet med en ukjent teknisk feil. message={}", e.getMessage(), e);
			throw e;
		}
	}

	private PepAnswer handleResponseFromTilgangsmaskinen(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
		try (response) {
			if (HttpStatus.FORBIDDEN.equals(response.getStatusCode())) {
				return translateToPepAnswer(response.bodyTo(TilgangsmaskinenDenyAnswer.class));
			} else if (HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
				return PepAnswer.permit();
			} else if (response.getStatusCode().is4xxClientError() && !HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
				throw new SafFunctionalException("Kall mot tilgangsmaskinen feilet med status " +
												 response.getStatusCode() + " " + response.getStatusText(), response.getStatusCode());
			} else {
				throw new SafTechnicalException("kall mot tilgangsmaskinen fikk uventet status " +
												response.getStatusCode() + " " + response.getStatusText(), response.getStatusCode());
			}
		}
	}

	private PepAnswer translateToPepAnswer(TilgangsmaskinenDenyAnswer tilgangsmaskinenDenyAnswer) {
		if (tilgangsmaskinenDenyAnswer == null) {
			return deny(new UkjentEllerTekniskReason());
		}
		if (tilgangsmaskinenDenyAnswer.status() == 204) {
			return PepAnswer.permit();
		}

		return switch (tilgangsmaskinenDenyAnswer.title()) {
			case "AVVIST_HABILITET" ->
					deny(new HabilitetReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse())); // informasjon om deg selv / familie
			case "AVVIST_SKJERMING" ->
					deny(new EgenAnsattReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse())); // informasjon om andre nav ansatte
			case "AVVIST_GEOGRAFISK" ->
					deny(new GeografiReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case "AVVIST_FORTROLIG_ADRESSE" ->
					deny(new FortroligAdresseReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case "AVVIST_STRENGT_FORTROLIG_ADRESSE" ->
					deny(new StrengtFortroligAdresseReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case "AVVIST_STRENGT_FORTROLIG_UTLAND" ->
					deny(new StrengtFortroligAdresseUtlandReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case "AVVIST_PERSON_UTLAND" ->
					deny(new PersonUtlandReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));

			default -> {
				log.warn("pep1g kunne ikke matche tilgangsmaskinen-response til DenyReason. title/avvisningskode={}", tilgangsmaskinenDenyAnswer.title());
				yield deny(new UkjentEllerTekniskReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			}
		};
	}
}
