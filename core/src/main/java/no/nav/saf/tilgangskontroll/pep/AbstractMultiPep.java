package no.nav.saf.tilgangskontroll.pep;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.slf4j.MDC;

import java.net.http.HttpTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenConsumer.READ_TIMEOUT;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Slf4j
public abstract class AbstractMultiPep<T> extends Pep<T> {

	private static final long OPPSLAG_TIMEOUT_SEKUNDER = 3;

	private final Pep<T> abacBackedPep;
	private final Pep<T> tilgangsmaskinenBackedPep;
	private final boolean featureToggleUseCheckTilgangsmaskinen;
	private final boolean prioritizeTilgangsmaskinenAnswer;
	private final String pepName;

	protected AbstractMultiPep(Pep<T> abacBackedPep,
							   Pep<T> tilgangsmaskinenBackedPep,
							   boolean featureToggleUseCheckTilgangsmaskinen,
							   boolean prioritizeTilgangsmaskinenAnswer,
							   String pepName) {
		this.abacBackedPep = abacBackedPep;
		this.tilgangsmaskinenBackedPep = tilgangsmaskinenBackedPep;
		this.featureToggleUseCheckTilgangsmaskinen = featureToggleUseCheckTilgangsmaskinen;
		this.prioritizeTilgangsmaskinenAnswer = prioritizeTilgangsmaskinenAnswer;
		this.pepName = pepName;
	}

	@Override
	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		var currentMdcContextMap = MDC.getCopyOfContextMap();
		CompletableFuture<PepAnswer> abacSaf = CompletableFuture.supplyAsync(() -> {
					MDC.setContextMap(currentMdcContextMap);
					return abacBackedPep.hasAccessWithAnswer(ressurs, safRequestContext);
				})
				.orTimeout(OPPSLAG_TIMEOUT_SEKUNDER, SECONDS)
				.handle(handleExceptionInOppslag("abac-saf", currentMdcContextMap));

		if (featureToggleUseCheckTilgangsmaskinen) {
			CompletableFuture<PepAnswer> tilgangsmaskinen = CompletableFuture.supplyAsync(() -> {
						MDC.setContextMap(currentMdcContextMap);
						return tilgangsmaskinenBackedPep.hasAccessWithAnswer(ressurs, safRequestContext);
					})
					.orTimeout(OPPSLAG_TIMEOUT_SEKUNDER, SECONDS)
					.handle(handleExceptionInOppslag("tilgangsmaskinen", currentMdcContextMap));

			return analyzeLogAndChoosePepAnswer(abacSaf.join(), tilgangsmaskinen.join());
		} else {
			return abacSaf.join();
		}
	}

	private BiFunction<PepAnswer, Throwable, PepAnswer> handleExceptionInOppslag(String serviceName, Map<String, String> mdcContextMap) {
		return (pepanswer, error) -> {
			if (error != null) {
				MDC.setContextMap(mdcContextMap);

				switch (error) {
					case CompletionException ignored when getRootCause(error) instanceof CallNotPermittedException ->
							log.error("{}: Oppslag mot {} ble ikke utført. Circuitbreaker er åpen på grunn av timeout eller høy feilrate",
									pepName, serviceName, error.getCause());
					case CompletionException ignored when getRootCause(error) instanceof HttpTimeoutException ->
							log.warn("{}: Oppslag mot {} feilet med timeout (tok over {} sekunder)",
									pepName, serviceName, READ_TIMEOUT.getSeconds());
					case TimeoutException ignored ->
							log.warn("{}: Oppslag mot {} feilet med timeout (tok over {} sekunder)",
									pepName, serviceName, OPPSLAG_TIMEOUT_SEKUNDER);
					default -> log.error("{}: Oppslag mot {} feilet uventet", pepName, serviceName, error);
				}
				return null;
			}
			return pepanswer;
		};
	}

	private PepAnswer analyzeLogAndChoosePepAnswer(PepAnswer abacAnswer, PepAnswer tilgangsmaskinenAnswer) {
		if (abacAnswer == null && tilgangsmaskinenAnswer == null) {
			log.error("{}: Både oppslag mot abac og tilgangsmaskinen feilet. Returnerer deny", pepName);
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		if (tilgangsmaskinenAnswer == null) {
			if (prioritizeTilgangsmaskinenAnswer) {
				log.error("{}: oppslag mot tilgangsmaskinen feilet, og Multi{} er satt til å prioritere svar fra tilgangsmaskinen. Returnerer deny", pepName, pepName);
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			} else {
				log.warn("{}: oppslag mot tilgangsmaskinen feilet, men Multi{} er satt til å prioritere svar fra abac. Returnerer abac={}",
						pepName, pepName, abacAnswer.isPermit() ? "PERMIT" : abacAnswer.getPepDenyReason().getAbacDenyReasonCode());
				return abacAnswer;
			}
		}
		if (abacAnswer == null) {
			if (prioritizeTilgangsmaskinenAnswer) {
				log.warn("{}: oppslag mot abac feilet, men Multi{} er satt til å prioritere svar fra tilgangsmaskinen. Returnerer tilgangsmaskinen={}",
						pepName, pepName, tilgangsmaskinenAnswer.isPermit() ? "PERMIT" :
								tilgangsmaskinenAnswer.getPepDenyReason().getRawTilgangsmaskinenDenyReason() + "(mappet til " + tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode() + ")");
				return tilgangsmaskinenAnswer;
			} else {
				log.error("{}: oppslag mot abac feilet, og Multi{} er satt til å prioritere svar fra abac. Returnerer deny", pepName, pepName);
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			}
		}

		if (abacAnswer.isPermit() && tilgangsmaskinenAnswer.isPermit()) {
			log.debug("{}: abac og tilgangsmaskinen er enige om permit", pepName);
			return PepAnswer.permit();
		}

		PepAnswer finalAnswer = prioritizeTilgangsmaskinenAnswer ? tilgangsmaskinenAnswer : abacAnswer;
		if (abacAnswer.isPermit()) {
			log.warn("{}: abac og tilgangsmaskinen er uenige: abac=PERMIT tilgangsmaskinen={}. Multi{} er satt til å prioritere {} og returnerer {}",
					pepName, tilgangsmaskinenAnswer.getPepDenyReason().getRawTilgangsmaskinenDenyReason() + "(mappet til " + tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode() + ")",
					pepName, prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getDecision());
		} else if (tilgangsmaskinenAnswer.isPermit()) {
			log.warn("{}: abac og tilgangsmaskinen er uenige: abac={} tilgangsmaskinen=PERMIT. Multi{} er satt til å prioritere {} og returnerer {}",
					pepName, abacAnswer.getPepDenyReason().getAbacDenyReasonCode(), pepName,
					prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getDecision());
		} else {
			if (abacAnswer.getPepDenyReason().getAbacDenyReasonCode() == tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode()) {
				log.debug("{}: abac og tilgangsmaskinen er enige om deny", pepName);
			} else {
				log.info("{}: abac og tilgangsmaskinen er enige om deny, men uenige om hvorfor: abac={} tilgangsmaskinen={}. Multi{} er satt til å prioritere {} og returnerer DENY: {}",
						pepName, abacAnswer.getPepDenyReason().getAbacDenyReasonCode(),
						tilgangsmaskinenAnswer.getPepDenyReason().getRawTilgangsmaskinenDenyReason() + "(mappet til " + tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode() + ")",
						pepName, prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getPepDenyReason().getAbacDenyReasonCode());
			}
		}
		return finalAnswer;
	}

	@Override
	PepAnswer verifyAzureClientCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	PepAnswer verifyRestSTSCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext) {
		throw new UnsupportedOperationException();
	}
}

