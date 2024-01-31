package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.ABAC_JOURNALSTATUS_UTGAAR;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_JOURNALSTATUS;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_JOURNAL_METADATA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Journalpoststatus
 */
@Slf4j
@Component(PEP4)
public class Pep4Impl extends Pep<TilgangJournalpost> {

	private final AbacService abacService;

	@Autowired
	public Pep4Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public AbacAnswer verifyAbacPdpDecision(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep4 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll.");
			return AbacAnswer.deny(AbacAnswer.AbacDenyReasonCode.UKJENT);
		}

		if (isJournalpoststatusUtgaar(ressurs) || isSkjermingPresent(ressurs)) {
			return hasJournalpostAccess(safRequestContext, ressurs);
		} else {
			return AbacAnswer.permit();
		}
	}

	private AbacAnswer hasJournalpostAccess(SafRequestContext safRequestContext, TilgangJournalpost ressurs) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA);

		if (isJournalpoststatusUtgaar(ressurs)) {
			request.resource(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR);
		}
		if (isSkjermingPresent(ressurs)) {
			request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());
		}

		traceLogPepStarted(PEP4, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP4, ressurs);

		return mapXacmlResponse(response);
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		return permit();
	}

	@Override
	AbacAnswer.AbacDenyReasonCode translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		return AbacAnswer.AbacDenyReasonCode.JOURNALSTATUS;
	}

	private boolean isJournalpoststatusUtgaar(TilgangJournalpost ressurs) {
		return Journalstatus.UTGAAR.equals(ressurs.getJournalstatus());
	}

	private boolean isSkjermingPresent(TilgangJournalpost ressurs) {
		return ressurs.getSkjerming() != null;
	}

}
