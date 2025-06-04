package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.AvsluttetSakReason;
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
			PepAnswer permit = permit();
			safRequestContext.getRequestCache().putDecision(KeyGeneratorLocalCaching.getKeyForPep8d(null, null), permit);
			return permit;
		}

		if (ressurs.isAvsluttet()) {
			if (!navUserGroupMembershipService.isNavIdentInJoarkHistoriskGroup(safRequestContext.getUserId())) {
				return PepAnswer.deny(new AvsluttetSakReason());
			}
		}

		PepAnswer permit = permit();
		safRequestContext.getRequestCache().putDecision(KeyGeneratorLocalCaching.getKeyForPep8d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer()), permit);
		return permit;
	}
}
