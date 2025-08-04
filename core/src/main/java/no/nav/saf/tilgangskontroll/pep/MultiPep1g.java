package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static no.nav.saf.domain.DomainConstants.PEP1G;

@Slf4j
@Component(PEP1G)
public class MultiPep1g extends Pep<TilgangBruker> {

	private static final long OPPSLAG_TIMEOUT_MILLIS = 1000;
	private final AbacBackedPep1gImpl abacBackedPep;
	private final TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenBackedPep;
	private final boolean featureToggleUseCheckTilgangsmaskinen;
	private final boolean prioritizeTilgangsmaskinenAnswer;

	public MultiPep1g(AbacBackedPep1gImpl abacBackedPep, TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenBackedPep,
					  @Value("${saf.pep1g.feature_toggle_tilgangsmaskinen}") boolean featureToggleUseCheckTilgangsmaskinen,
					  @Value("${saf.pep1g.prioritize_tilgangsmaskinen}") boolean prioritizeTilgangsmaskinenAnswer) {
		this.abacBackedPep = abacBackedPep;
		this.tilgangsmaskinenBackedPep = tilgangsmaskinenBackedPep;
		this.featureToggleUseCheckTilgangsmaskinen = featureToggleUseCheckTilgangsmaskinen;
		this.prioritizeTilgangsmaskinenAnswer = prioritizeTilgangsmaskinenAnswer;
	}

	@Override
	public PepAnswer hasAccessWithAnswer(TilgangBruker ressurs, SafRequestContext safRequestContext) {

		CompletableFuture<PepAnswer> abacSaf = CompletableFuture.supplyAsync(() ->
						abacBackedPep.hasAccessWithAnswer(ressurs, safRequestContext))
				.orTimeout(OPPSLAG_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
				.handle(handleExceptionInOppslag("abac-saf"));

		if (featureToggleUseCheckTilgangsmaskinen) {
			CompletableFuture<PepAnswer> tilgangsmaskinen = CompletableFuture.supplyAsync(() ->
							tilgangsmaskinenBackedPep.hasAccessWithAnswer(ressurs, safRequestContext))
					.orTimeout(OPPSLAG_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
					.handle(handleExceptionInOppslag("tilgangsmaskinen"));

			return analyzeLogAndChoosePepAnswer(abacSaf.join(), tilgangsmaskinen.join());
		} else {
			return abacSaf.join();
		}
	}

	private static BiFunction<PepAnswer, Throwable, PepAnswer> handleExceptionInOppslag(String name) {
		return (pepanswer, error) -> {
			if (error != null) {
				if (error instanceof TimeoutException) {
					log.warn("PEP1g: Oppslag mot {} feilet med timeout (tok over {} millisekunder)", name, OPPSLAG_TIMEOUT_MILLIS);
				} else {
					log.error("PEP1g: Oppslag mot {} feilet uventet", name, error);
				}
				return null;
			}
			return pepanswer;
		};
	}

	private PepAnswer analyzeLogAndChoosePepAnswer(PepAnswer abacAnswer, PepAnswer tilgangsmaskinenAnswer) {
		if (tilgangsmaskinenAnswer == null) {
			if (prioritizeTilgangsmaskinenAnswer) {
				log.error("PEP1g: oppslag mot tilgangsmaskinen feilet, og multipep1g er satt til å prioritere svar fra tilgangsmaskinen. Returnerer deny");
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			} else {
				log.warn("PEP1g: oppslag mot tilgangsmaskinen feilet, men multipep1g er satt til å prioritere svar fra abac. Returnerer abac={}",
						abacAnswer.isPermit() ? "PERMIT" : abacAnswer.getPepDenyReason().getAbacDenyReasonCode());
				return abacAnswer;
			}
		}
		if (abacAnswer == null) {
			if (prioritizeTilgangsmaskinenAnswer) {
				log.warn("PEP1g: oppslag mot abac feilet, men multipep1g er satt til å prioritere svar fra tilgangsmaskinen. Returnerer tilgangsmaskinen={}",
						tilgangsmaskinenAnswer.isPermit() ? "PERMIT" : tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode());
				return tilgangsmaskinenAnswer;
			} else {
				log.error("PEP1g: oppslag mot abac feilet, og multipep1g er satt til å prioritere svar fra abac. Returnerer deny");
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			}
		}

		if (abacAnswer.isPermit() && tilgangsmaskinenAnswer.isPermit()) {
			log.debug("PEP1g: abac og tilgangsmaskinen er enige om permit");
			return PepAnswer.permit();
		}

		PepAnswer finalAnswer = prioritizeTilgangsmaskinenAnswer ? tilgangsmaskinenAnswer : abacAnswer;
		if (abacAnswer.isPermit()) {
			log.warn("PEP1g: abac og tilgangsmaskinen er uenige: abac=PERMIT tilgangsmaskinen={}. Multipep1g er satt til å prioritere {} og returnerer {}",
					tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode(), prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getDecision());
		} else if (tilgangsmaskinenAnswer.isPermit()) {
			log.warn("PEP1g: abac og tilgangsmaskinen er uenige: abac={} tilgangsmaskinen=PERMIT. Multipep1g er satt til å prioritere {} og returnerer {}",
					abacAnswer.getPepDenyReason().getAbacDenyReasonCode(), prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getDecision());
		} else {
			if (abacAnswer.getPepDenyReason().getAbacDenyReasonCode() == tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode()) {
				log.debug("PEP1g: abac og tilgangsmaskinen er enige om deny");
			} else {
				log.info("PEP1g: abac og tilgangsmaskinen er enige om deny, men uenige om hvorfor: abac={} tilgangsmaskinen={}. Multipep1g er satt til å prioritere {} og returnerer DENY: {}",
						abacAnswer.getPepDenyReason().getAbacDenyReasonCode(), tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode(),
						prioritizeTilgangsmaskinenAnswer ? "tilgangsmaskinen" : "abac", finalAnswer.getPepDenyReason().getAbacDenyReasonCode());
			}
		}
		return finalAnswer;
	}

	@Override
	PepAnswer verifyAzureClientCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		// Denne skal aldri bli kalt fordi vi delegerer til de underliggende pep-ene
		throw new UnsupportedOperationException();
	}

	@Override
	PepAnswer verifyRestSTSCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		// Denne skal aldri bli kalt fordi vi delegerer til de underliggende pep-ene
		throw new UnsupportedOperationException();
	}

}
