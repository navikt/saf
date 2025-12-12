package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattPartReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdressePartReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdressePartReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandPartReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
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
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.EGEN_ANSATT;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Slf4j
@Component
public class AbacBackedPep7dImpl extends StandardAbacBackedPep<TilgangSak> {

	private final AbacService abacService;

	@Autowired
	public AbacBackedPep7dImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	private final List<Tema> relevanteTemaK9 = Arrays.asList(FRI, OMS);

	@Override
	public PepAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {

		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());

			if (FOR.equals(ressurs.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForFp(ressurs)) {
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
					return PepAnswer.permit();
				}

				if (safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getFpAktoerIdList());
					PepAnswer pepAnswer = mapToAbacAnswer(response);
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
					return pepAnswer;
				}

				return safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching);
			}

			if (relevanteTemaK9.contains(ressurs.getTema()) && FAGSAKSYSTEM_K9.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForK9(ressurs)) {
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
					return PepAnswer.permit();
				}
				if (safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getK9AktoerIdList());
					PepAnswer pepAnswer = mapToAbacAnswer(response);
					safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
					return pepAnswer;
				}

				return safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching);
			}
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
		}
		return PepAnswer.permit();
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
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
		}
		return permit();
	}

	@Override
	protected PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		var advices = xacmlResponse.getAdvicesMap();

		if (EGEN_ANSATT.matchesAbacAdvice(advices)) {
			return deny(new EgenAnsattPartReason(advices));
		} else if (FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new FortroligAdressePartReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdressePartReason(advices));
		} else if (STRENGT_FORTROLIG_ADRESSE_UTLAND.matchesAbacAdvice(advices)) {
			return deny(new StrengtFortroligAdresseUtlandPartReason(advices));
		}
		log.warn("pep7 kunne ikke matche abac-response til DenyReason advices={}", advices);
		return deny(new UkjentEllerTekniskReason());
	}
}
