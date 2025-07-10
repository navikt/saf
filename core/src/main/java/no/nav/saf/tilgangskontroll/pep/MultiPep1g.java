package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP1G;

@Slf4j
@Component(PEP1G)
public class MultiPep1g extends Pep<TilgangBruker> {

	private final AbacBackedPep1gImpl abacBackedPep;
	private final TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenBackedPep;
	private final boolean prioritizeTilgangsmaskinenAnswer;

	public MultiPep1g(AbacBackedPep1gImpl abacBackedPep, TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenBackedPep,
					  @Value("${saf.pep1g.prioritize_tilgangsmaskinen}") boolean prioritizeTilgangsmaskinenAnswer) {
		this.abacBackedPep = abacBackedPep;
		this.tilgangsmaskinenBackedPep = tilgangsmaskinenBackedPep;
		this.prioritizeTilgangsmaskinenAnswer = prioritizeTilgangsmaskinenAnswer;
	}

	@Override
	public PepAnswer hasAccessWithAnswer(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		var abacAnswer = abacBackedPep.hasAccessWithAnswer(ressurs, safRequestContext);
		var tilgangsmaskinenAnswer = tilgangsmaskinenBackedPep.hasAccessWithAnswer(ressurs, safRequestContext);

		return analyzeLogAndChoosePepAnswer(abacAnswer, tilgangsmaskinenAnswer);
	}

	private PepAnswer analyzeLogAndChoosePepAnswer(PepAnswer abacAnswer, PepAnswer tilgangsmaskinenAnswer) {
		if (abacAnswer.isPermit() && tilgangsmaskinenAnswer.isPermit()) {
			log.debug("PEP1g: abac og tilgangsmaskinen er enige om permit");
			return PepAnswer.permit();
		}
		if (abacAnswer.isPermit()) {
			log.warn("PEP1g: abac og tilgangsmaskinen er uenige: abac=PERMIT tilgangsmaskinen={}", tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode());
		} else if (tilgangsmaskinenAnswer.isPermit()) {
			log.warn("PEP1g: abac og tilgangsmaskinen er uenige: abac={} tilgangsmaskinen=PERMIT", abacAnswer.getPepDenyReason().getAbacDenyReasonCode());
		} else {
			if (abacAnswer.getPepDenyReason().getAbacDenyReasonCode() == tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode()) {
				log.debug("PEP1g: abac og tilgangsmaskinen er enige om deny");
			} else {
				log.info("PEP1g: abac og tilgangsmaskinen er enige om deny, men uenige om hvorfor: abac={} tilgangsmaskinen={}",
						abacAnswer.getPepDenyReason().getAbacDenyReasonCode(), tilgangsmaskinenAnswer.getPepDenyReason().getAbacDenyReasonCode());
			}
		}
		return prioritizeTilgangsmaskinenAnswer ? tilgangsmaskinenAnswer : abacAnswer;
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
