package no.nav.saf.anticorruptionlayer.nav;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.integration.azure.OboToken;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;

import static java.time.Duration.ofSeconds;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;

@Slf4j
@Component
public class TilgangsmaskinenConsumer {

	private final WebClient webClient;

	public TilgangsmaskinenConsumer(WebClient webClient, SafProperties safProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getTilgangsmaskinen().getUrl())
				.build();
	}

	public PepAnswer navIdentHasAccess(String identifikator, OboToken navIdentOboToken) {
		var tilgangsmaskinenResponse = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/api/v1/komplett").build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(5));
					reactorRequest.header("Authorization", "Bearer " + navIdentOboToken.token());
				})
				.bodyValue(identifikator)
				.retrieve()
				.onStatus(HttpStatus.FORBIDDEN::equals, response ->
						response.bodyToMono(TilgangsmaskinenDenyAnswer.class).map(TransportContainerException::new))
				.onStatus(((Predicate<HttpStatusCode>) HttpStatusCode::is5xxServerError).or(HttpStatus.NOT_FOUND::isSameCodeAs),
						cresponse -> Mono.error(new RetryableTilgangsmaskinenException()))
				.bodyToMono(TilgangsmaskinenDenyAnswer.class)
				.flatMapMany(Mono::just, exception -> {
					if (exception instanceof TransportContainerException tce) {
						return Mono.just(tce.response);
					}
					return Mono.error(exception);
				}, Mono::empty)
				.doOnError(Throwable.class, TilgangsmaskinenConsumer::logError)
				.retryWhen(Retry.backoff(3, Duration.ofSeconds(3)).filter(exception -> exception instanceof RetryableTilgangsmaskinenException))
				.blockFirst();
		if (tilgangsmaskinenResponse == null) {
			return PepAnswer.permit();
		}
		return translateToPepAnswer(tilgangsmaskinenResponse);
	}

	private static void logError(Throwable e) {
		switch (e) {
			case DecodingException decodingException ->
					log.error("Klarte ikke dekode payload fra Tilgangsmaskinen. Får ikke lastet cache. message={}",
							e.getMessage(), e);
			case WebClientException webClientException ->
					log.error("Kall mot Tilgangsmaskinen feilet. Får ikke lastet cache. message={}", e.getMessage(), e);
			case null, default ->
					log.error("Kall mot Tilgangsmaskinen feilet med en ukjent teknisk feil. message={}", e == null ? "null" : e.getMessage(), e);
		}
	}

	class TransportContainerException extends RuntimeException {
		private final TilgangsmaskinenDenyAnswer response;

		TransportContainerException(TilgangsmaskinenDenyAnswer response) {
			this.response = response;
		}
	}

	protected PepAnswer translateToPepAnswer(TilgangsmaskinenDenyAnswer tilgangsmaskinenDenyAnswer) {
		if (tilgangsmaskinenDenyAnswer.status() == 204) {
			return PepAnswer.permit();
		}

		var advices = Map.of("deny_policy", tilgangsmaskinenDenyAnswer.begrunnelse());
		return switch (tilgangsmaskinenDenyAnswer.title()) {
			case AVVIST_HABILITET -> deny(new EgenAnsattReason(advices)); // informasjon om deg selv / familie
			case AVVIST_SKJERMING -> deny(new EgenAnsattReason(advices)); // informasjon om andre nav ansatte
			case AVVIST_GEOGRAFISK -> deny(new GeografiReason(advices));
			case AVVIST_FORTROLIG_ADRESSE -> deny(new FortroligAdresseReason(advices));
			case AVVIST_STRENGT_FORTROLIG_ADRESSE -> deny(new StrengtFortroligAdresseReason(advices));
			case AVVIST_STRENGT_FORTROLIG_UTLAND -> deny(new StrengtFortroligAdresseUtlandReason(advices));

			default -> {
				log.warn("pep1g kunne ikke matche tilgangsmaskinen-response til DenyReason. title/avvisningskode={}", tilgangsmaskinenDenyAnswer.title());
				yield deny(new UkjentEllerTekniskReason());
			}
		};
	}
}
