package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static no.nav.saf.domain.DomainConstants.PEP7D;
import static no.nav.saf.domain.kode.Tema.FOR;
import static no.nav.saf.domain.kode.Tema.FRI;
import static no.nav.saf.domain.kode.Tema.OMS;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_TREDJEPART;
import static no.nav.saf.tilgangskontroll.abac.dto.response.AdviceStringUtil.getAdvicesMap;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Slf4j
@Component(PEP7D)
public class Pep7dImpl extends Pep<TilgangSak> {

	private final AbacService abacService;

	@Autowired
	public Pep7dImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	private final List<Tema> relevanteTemaK9 = Arrays.asList(FRI, OMS);

	@Override
	public AbacAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {

		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());

			if (FOR.equals(ressurs.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForFp(ressurs)) {
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
					return AbacAnswer.permit();
				}

				if (safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getFpAktoerIdList());
					// hvordan kan man få mer info ut av denne booleanen - kan man gjøre om på denne kanskje?
					AbacAnswer abacAnswer = mapXacmlResponse(response);
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, abacAnswer);
					return abacAnswer;
				}

				return safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching);
			}

			if (relevanteTemaK9.contains(ressurs.getTema()) && FAGSAKSYSTEM_K9.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForK9(ressurs)) {
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
					return AbacAnswer.permit();
				}
				if (safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getK9AktoerIdList());
					AbacAnswer abacAnswer = mapXacmlResponse(response);
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, abacAnswer);
					return abacAnswer;
				}

				return safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching);
			}
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
		}
		return AbacAnswer.permit();
	}

	private boolean aktoerlisteErNullEllerTomForFp(TilgangSak ressurs) {
		if (ressurs.getFpAktoerIdList() == null || ressurs.getFpAktoerIdList().isEmpty()) {
			log.info("Pep7d(kode6/7-relevante-parter) har ingen relevante parter for foreldrepengesak. Tilgang gis.");
			return true;
		}
		return false;
	}

	private boolean aktoerlisteErNullEllerTomForK9(TilgangSak ressurs) {
		if (ressurs.getK9AktoerIdList() == null || ressurs.getK9AktoerIdList().isEmpty()) {
			log.info("Pep7d(kode6/7-relevante-parter) har ingen relevante parter for K9sak. Tilgang gis.");
			return true;
		}
		return false;
	}

	private XacmlResponse getXacmlResponseFromAbac(TilgangSak ressurs, SafRequestContext safRequestContext, List<String> aktoerIdList) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
		aktoerIdList.forEach(aktoerId -> request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktoerId));

		traceLogPepStarted(PEP7D, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP7D, ressurs);

		return response;
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
		}
		return permit();
	}

	@Override
	AbacAnswer.AbacDenyReasonCode translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		var adviceMap = getAdvicesMap(xacmlResponse.getAdvices());
		return switch ((adviceMap.get("deny_policy") + ":" + adviceMap.get("deny_rule")).toLowerCase()) {
			// subset av statuser; dette er alle som kan mappes ut i pep7d
			case "skjermede_navansatte_og_familiemedlemmer:behandle_skjermede_navansatte_og_familiemedlemmer_mangler_gruppetilgang" ->
					AbacAnswer.AbacDenyReasonCode.EGEN_ANSATT_PART;
			case "adressebeskyttelse_fortrolig_adresse:fortrolig_adresse_nok" ->
					AbacAnswer.AbacDenyReasonCode.FORTROLIG_ADRESSE_PART;
			case "adressebeskyttelse_strengt_fortrolig_adresse:strengt_fortrolig_adresse_nok" ->
					AbacAnswer.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_PART;
			case "adressebeskyttelse_strengt_fortrolig_adresse_utland:strengt_fortrolig_adresse_utland_nok" ->
					AbacAnswer.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND_PART;
			default -> AbacAnswer.AbacDenyReasonCode.UKJENT;

		};
	}
}
