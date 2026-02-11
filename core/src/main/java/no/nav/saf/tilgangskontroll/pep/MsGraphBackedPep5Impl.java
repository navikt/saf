package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.SkjermingReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 */
@Slf4j
@Component(PEP5)
public class MsGraphBackedPep5Impl extends StandardMsGraphBackedPep<TilgangDokumentInfo> {

	private final NavUserGroupMembershipService navUserGroupMembershipService;

	public MsGraphBackedPep5Impl(NavUserGroupMembershipService navUserGroupMembershipService) {
		this.navUserGroupMembershipService = navUserGroupMembershipService;
	}

	@Override
	PepAnswer verifyNavIdentGroupMembershipAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
		if (isSkjermingPresent(ressurs)) {

			boolean decision = navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(safRequestContext.getUserId());
			PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new SkjermingReason(
					"dokument_info_skjermet", "saf_pep5", "dokument_info_skjermet"
			));

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} else {
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
			return PepAnswer.permit();
		}
	}

	@Override
	public PepAnswer verifyAccessForSystem(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return PepAnswer.deny(new UkjentEllerTekniskReason(
			"mangler_data", "saf_pep5", "dokument_info_er_null"
			));
		}
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
		boolean decision = !isSkjermingPresent(ressurs);
		PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new SkjermingReason(
				"dokument_info_skjermet", "saf_pep5", "dokument_info_skjermet"
		));
		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		return pepAnswer;
	}

	private boolean isSkjermingPresent(TilgangDokumentInfo ressurs) {
		return ressurs.getSkjerming() != null;
	}
}
