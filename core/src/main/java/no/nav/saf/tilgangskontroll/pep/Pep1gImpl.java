package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.OrgnrNavStatReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_PERSON;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.EGEN_ANSATT;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.GEOGRAFI;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 * https://confluence.adeo.no/display/ABAC/FP3%3A+Egen+ansatt
 */
@Component(PEP1G)
@Slf4j
public class Pep1gImpl extends StandardPep<TilgangBruker> {

	public static final String ORGANISASJON_ER_NAV_STAT_KREVER_EGEN_ANSATT_TILGANG = "organisasjon_er_nav_stat_krever_egen_ansatt_tilgang";
	private final AbacService abacService;
	private final NavOrgService navOrgService;

	@Autowired
	public Pep1gImpl(AbacService abacService,
					 NavOrgService navOrgService) {
		this.abacService = abacService;
		this.navOrgService = navOrgService;
	}

	@Override
	public AbacAnswer verifyAbacPdpDecision(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.isUkjent()) {
			log.info("Pep1g(kode6/7, egen-ansatt, geografi) mangler data om bruker. Tilgang gis for å kunne identifisere bruker.");
			return AbacAnswer.permit();
		} else if (ressurs.isOrganisasjon()) {
			return verifyTilgangOrganisasjon(ressurs.getOrgnummer(), safRequestContext);
		}

		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON);

		if (ressurs.getAktoerId() != null) {
			request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, ressurs.getAktoerId());
		} else if (ressurs.getFoedselsnr() != null) {
			request.resource(RESOURCE_FELLES_PERSON_FNR, ressurs.getFoedselsnr());
		} else {
			log.error("Pep1g kunne ikke validere bruker fordi bruker ikke er en person. Denne tilstanden indikerer en teknisk feil.");
			return deny(new UkjentEllerTekniskReason());
		}
		traceLogPepStarted(PEP1G, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP1G, ressurs);
		return mapToAbacAnswer(response);
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			return permit();
		} else if (ressurs.isOrganisasjon()) {
			return verifyTilgangOrganisasjon(ressurs.getOrgnummer(), safRequestContext);
		}
		return permit();
	}

	@Override
	protected AbacAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		Map<String, String> advices = xacmlResponse.getAdvicesMap();

		if (EGEN_ANSATT.matchesAbacAdvice(advices)) {
			return deny(new EgenAnsattReason(advices));
		} else if (GEOGRAFI.matchesAbacAdvice(advices)) {
			return deny(new GeografiReason(advices));
		} else if (FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new FortroligAdresseReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdresseReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE_UTLAND.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdresseUtlandReason(advices));
		}
		log.warn("pep1g kunne ikke matche abac-response til DenyReason advices={}", advices);
		return deny(new UkjentEllerTekniskReason());
	}


	private AbacAnswer verifyTilgangOrganisasjon(String organisasjonsnummer, SafRequestContext safRequestContext) {
		if (!safRequestContext.isUserIdNavAnsatt()) {
			return AbacAnswer.permit();
		}
		if (navOrgService.isOrganisasjonsnummerNavBedrift(organisasjonsnummer)) {
			log.info("Pep1g organisasjonsnummer={} er en NAV Organisasjon. Undersøker om NAV ansatt har tilgang.", organisasjonsnummer);
			if (navOrgService.isNavIdentInEgenAnsattGroup(safRequestContext.getUserId())) {
				return AbacAnswer.permit();
			}
			return deny(new OrgnrNavStatReason(
					"", "skjermede_navansatte_og_familiemedlemmer", "behandle_skjermede_navansatte_og_familiemedlemmer_mangler_gruppetilgang"
			));
		}
		return AbacAnswer.permit();
	}
}
