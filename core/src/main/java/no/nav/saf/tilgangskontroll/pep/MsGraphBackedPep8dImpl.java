package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.AvsluttetSakReason;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP8D;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

@Slf4j
@Component(PEP8D)
public class MsGraphBackedPep8dImpl extends StandardMsGraphBackedPep<TilgangSak> {

	private final NavUserGroupMembershipService navUserGroupMembershipService;

	public MsGraphBackedPep8dImpl(NavUserGroupMembershipService navUserGroupMembershipService) {
		this.navUserGroupMembershipService = navUserGroupMembershipService;
	}

	@Override
	PepAnswer verifyNavIdentGroupMembershipAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			return cacheAndReturnPermit(safRequestContext, null, null);
		}

		if (ressurs.isAvsluttet()) {
			if (!navUserGroupMembershipService.isNavIdentInJoarkHistoriskGroup(safRequestContext.getUserId())) {
				return PepAnswer.deny(new AvsluttetSakReason());
			}
		}

		return cacheAndReturnPermit(safRequestContext, ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
	}

	@Override
	protected PepAnswer verifyAccessForSystemUser(TilgangSak ressurs, SafRequestContext safRequestContext) {
		return cacheAndReturnPermit(safRequestContext, ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
	}

	private static PepAnswer cacheAndReturnPermit(SafRequestContext safRequestContext, Arkivsakssystem arkivsakssystem, String arkivsaknummer) {
		PepAnswer permit = permit();
		safRequestContext.getRequestCache().putDecision(KeyGeneratorLocalCaching.getKeyForPep8d(arkivsakssystem, arkivsaknummer), permit);
		return permit;
	}
}
