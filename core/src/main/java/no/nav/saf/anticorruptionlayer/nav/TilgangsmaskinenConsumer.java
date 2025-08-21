package no.nav.saf.anticorruptionlayer.nav;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TOKEN_TO_EXCHANGE;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;

@Slf4j
@Component
public class TilgangsmaskinenConsumer {

	private static final String TILGANGSMASKINEN_INSTANCE = "tilgangsmaskinen";

	private final RestClient texasRestClient;
	private final SafProperties safProperties;

	public TilgangsmaskinenConsumer(RestClient texasRestClient, SafProperties safProperties) {
		this.texasRestClient = texasRestClient.mutate()
				.baseUrl(safProperties.getEndpoints().getTilgangsmaskinen().getUrl())
				.build();
		this.safProperties = safProperties;
	}

	@Retry(name = TILGANGSMASKINEN_INSTANCE)
	@CircuitBreaker(name = TILGANGSMASKINEN_INSTANCE)
	public PepAnswer navIdentHasAccess(String identifikator, JwtToken entraIdToken) {
		try {
			return texasRestClient.post()
					.uri(uriBuilder -> uriBuilder.path("/api/v1/komplett").build())
					.attributes(attributes -> {
						attributes.put(TARGET_SCOPE, safProperties.getEndpoints().getTilgangsmaskinen().getScope());
						attributes.put(TOKEN_TO_EXCHANGE, entraIdToken.getEncodedToken());
					})
					.body(identifikator)
					.exchange((request, response) -> handleResponseFromTilgangsmaskinen(response));
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

		var advices = Map.of("cause", tilgangsmaskinenDenyAnswer.title(), "deny_policy", tilgangsmaskinenDenyAnswer.begrunnelse());
		return switch (tilgangsmaskinenDenyAnswer.title()) {
			case "AVVIST_HABILITET" -> deny(new EgenAnsattReason(advices)); // informasjon om deg selv / familie
			case "AVVIST_SKJERMING" -> deny(new EgenAnsattReason(advices)); // informasjon om andre nav ansatte
			case "AVVIST_GEOGRAFISK" -> deny(new GeografiReason(advices));
			case "AVVIST_FORTROLIG_ADRESSE" -> deny(new FortroligAdresseReason(advices));
			case "AVVIST_STRENGT_FORTROLIG_ADRESSE" -> deny(new StrengtFortroligAdresseReason(advices));
			case "AVVIST_STRENGT_FORTROLIG_UTLAND" -> deny(new StrengtFortroligAdresseUtlandReason(advices));

			default -> {
				log.warn("pep1g kunne ikke matche tilgangsmaskinen-response til DenyReason. title/avvisningskode={}", tilgangsmaskinenDenyAnswer.title());
				yield deny(new UkjentEllerTekniskReason());
			}
		};
	}
}
