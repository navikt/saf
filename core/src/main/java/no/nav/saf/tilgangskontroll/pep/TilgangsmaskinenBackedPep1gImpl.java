package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.anticorruptionlayer.nav.TilgangsmaskinenConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.integration.token.NaisTexasConsumer;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.OrgnrNavStatReason;
import org.springframework.stereotype.Component;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Slf4j
@Component
public class TilgangsmaskinenBackedPep1gImpl extends StandardTilgangsmaskinenBackedPep {

	private final TilgangsmaskinenConsumer tilgangsmaskinenConsumer;
	private final NavOrgService navOrgService;
	private final NavUserGroupMembershipService navUserGroupMembershipService;
	private final NaisTexasConsumer naisTexasConsumer;

	public TilgangsmaskinenBackedPep1gImpl(NaisTexasConsumer naisTexasConsumer,
										   TilgangsmaskinenConsumer tilgangsmaskinenConsumer,
										   NavOrgService navOrgService,
										   NavUserGroupMembershipService navUserGroupMembershipService) {
		this.tilgangsmaskinenConsumer = tilgangsmaskinenConsumer;
		this.navOrgService = navOrgService;
		this.navUserGroupMembershipService = navUserGroupMembershipService;
		this.naisTexasConsumer = naisTexasConsumer;
	}

	@Override
	PepAnswer verifyNavIdentAccessToUser(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.isUkjent()) {
			log.info("Pep1g(kode6/7, egen-ansatt, geografi) mangler data om bruker. Tilgang gis for å kunne identifisere bruker.");
			return PepAnswer.permit();
		} else if (ressurs.isOrganisasjon()) {
			return verifyTilgangOrganisasjon(ressurs.getOrgnummer(), safRequestContext);
		} else {
			var oboToken = naisTexasConsumer.exchangeForTilgangsmaskinenOboToken(safRequestContext.getSecurityContext().getJwtToken());
			return tilgangsmaskinenConsumer.navIdentHasAccess(firstNonNull(ressurs.getAktoerId(), ressurs.getFoedselsnr()), oboToken);
		}
	}

	@Override
	public PepAnswer verifyAccessForSystemUser(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			return permit();
		} else if (ressurs.isOrganisasjon()) {
			return verifyTilgangOrganisasjon(ressurs.getOrgnummer(), safRequestContext);
		}
		return permit();
	}

	private PepAnswer verifyTilgangOrganisasjon(String organisasjonsnummer, SafRequestContext safRequestContext) {
		if (!safRequestContext.isUserIdNavAnsatt()) {
			return PepAnswer.permit();
		}
		if (navOrgService.isOrganisasjonsnummerNavBedrift(organisasjonsnummer)) {
			log.info("Pep1g organisasjonsnummer={} er en NAV Organisasjon. Undersøker om NAV ansatt har tilgang.", organisasjonsnummer);
			if (navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(safRequestContext.getUserId())) {
				return PepAnswer.permit();
			}
			return deny(new OrgnrNavStatReason(
					"", "skjermede_navansatte_og_familiemedlemmer", "behandle_skjermede_navansatte_og_familiemedlemmer_mangler_gruppetilgang"
			));
		}
		return PepAnswer.permit();
	}
}
