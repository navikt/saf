package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.SkjermingReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostDtoMapper.java
 */
@Slf4j
@Component(PEP6D)
public class MsGraphBackedPep6dImpl extends StandardMsGraphBackedPep<TilgangDokumentvariant> {

	private final NavUserGroupMembershipService navUserGroupMembershipService;

	@Autowired
	public MsGraphBackedPep6dImpl(NavUserGroupMembershipService navUserGroupMembershipService) {
		this.navUserGroupMembershipService = navUserGroupMembershipService;
	}

	@Override
	PepAnswer verifyNavIdentGroupMembershipAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			}

			traceLogPepStarted(PEP6D, ressurs);

			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			try {
				if (navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(safRequestContext.getUserId())) {
					PepAnswer permit = permit();
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, permit);
					return permit;
				}
				var response = PepAnswer.deny(new SkjermingReason("dokumentvariant_skjermet", "saf_pep6d", "dokumentvariant_skjermet"));
				safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, response);
				return response;
			} finally {
				traceLogPepFinished(PEP6D, ressurs);
			}
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			PepAnswer permit = permit();
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, permit);
			return permit;
		}
	}

	@Override
	protected PepAnswer verifyAccessForSystemUser(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return PepAnswer.deny(new SkjermingReason(
					"dokumentvariant_mangler_data", "saf_pep6d", "dokumentvariant_er_null"
			));
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}. Azure ccf.",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return PepAnswer.deny(
						new SkjermingReason("dokumentvariant_mangler_variantformat", "saf_pep6d", "dokumentvariant_skjermet_og_variantformat_er_null"));
			}

			traceLogPepStarted(PEP6D, ressurs);
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			traceLogPepFinished(PEP6D, ressurs);
			PepAnswer pepAnswer = PepAnswer.deny(new SkjermingReason(
					"dokumentvariant_skjermet", "saf_pep6d", "dokumentvariant_skjermet"));

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
			return permit();
		}
	}

	private boolean isSkjermingPresent(TilgangDokumentvariant ressurs) {
		return ressurs.getSkjerming() != null;
	}

	private boolean isVariantformatNull(TilgangDokumentvariant ressurs) {
		return ressurs.getVariantformat() == null;
	}

}
