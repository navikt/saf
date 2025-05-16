package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.JournalstatusReason;
import no.nav.saf.tilgangskontroll.pep.reasons.SkjermingReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyMap;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Journalpoststatus
 */
@Slf4j
@Component(PEP4)
public class MsGraphBackedPep4Impl extends StandardMsGraphBackedPep<TilgangJournalpost> {

	private final NavUserGroupMembershipService navUserGroupMembershipService;

	@Autowired
	public MsGraphBackedPep4Impl(NavUserGroupMembershipService navUserGroupMembershipService) {
		this.navUserGroupMembershipService = navUserGroupMembershipService;
	}

	@Override
	PepAnswer verifyNavIdentGroupMembershipAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep4 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll.");
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		if (isJournalpoststatusUtgaar(ressurs) || isSkjermingPresent(ressurs)) {
			return hasJournalpostAccess(safRequestContext, ressurs);
		} else {
			return PepAnswer.permit();
		}
	}

	private PepAnswer hasJournalpostAccess(SafRequestContext safRequestContext, TilgangJournalpost ressurs) {
		traceLogPepStarted(PEP4, ressurs);
		if (isJournalpoststatusUtgaar(ressurs)) {
			if (!navUserGroupMembershipService.isNavIdentInLeseUtgaatteDokumenterGroup(safRequestContext.getUserId())) {
				traceLogPepFinished(PEP4, ressurs);
				return PepAnswer.deny(new JournalstatusReason());
			}
		}
		if (isSkjermingPresent(ressurs)) {
			if (!navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(safRequestContext.getUserId())) {
				traceLogPepFinished(PEP4, ressurs);
				return PepAnswer.deny(new JournalstatusReason());
			}
		}
		if (!safRequestContext.isUserIdNavAnsatt()) {
			return PepAnswer.deny(new JournalstatusReason());
		}

		traceLogPepFinished(PEP4, ressurs);
		return permit();
	}

	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		return permit();
	}

	private boolean isJournalpoststatusUtgaar(TilgangJournalpost ressurs) {
		return Journalstatus.UTGAAR.equals(ressurs.getJournalstatus());
	}

	private boolean isSkjermingPresent(TilgangJournalpost ressurs) {
		return ressurs.getSkjerming() != null;
	}

}
