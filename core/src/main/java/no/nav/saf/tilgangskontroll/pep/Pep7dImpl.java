package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
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

	@Inject
	public Pep7dImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	private final List<Tema> relevanteTemaK9 = Arrays.asList(FRI, OMS);

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {

		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());

			if (FOR.equals(ressurs.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForFp(ressurs)) {
					safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
					return XacmlResponse.permit();
				}

				if (safRequestContext.getRequestCache().getObject(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getFpAktoerIdList());
					safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
					return response;
				}

				return safRequestContext.getRequestCache().getObject(tilgangKeyLocalCaching) ? XacmlResponse.permit() : XacmlResponse.deny();
			}

			if (relevanteTemaK9.contains(ressurs.getTema()) && FAGSAKSYSTEM_K9.equals(ressurs.getFagsaksystem())) {
				if (aktoerlisteErNullEllerTomForK9(ressurs)) {
					safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
					return XacmlResponse.permit();
				}
				if (safRequestContext.getRequestCache().getObject(tilgangKeyLocalCaching) == null) {
					XacmlResponse response = getXacmlResponseFromAbac(ressurs, safRequestContext, ressurs.getK9AktoerIdList());
					safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
					return response;
				}

				return safRequestContext.getRequestCache().getObject(tilgangKeyLocalCaching) ? XacmlResponse.permit() : XacmlResponse.deny();
			}
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
		}
		return XacmlResponse.permit();
	}

	private boolean aktoerlisteErNullEllerTomForFp(TilgangSak ressurs) {
		if (ressurs.getFpAktoerIdList() == null || ressurs.getFpAktoerIdList().isEmpty()) {
			log.info("Pep7d har ingen relevante parter for foreldrepengesak. Tilgang gis.");
			return true;
		}
		return false;
	}

	private boolean aktoerlisteErNullEllerTomForK9(TilgangSak ressurs) {
		if (ressurs.getK9AktoerIdList() == null || ressurs.getK9AktoerIdList().isEmpty()) {
			log.info("Pep7d har ingen relevante parter for K9sak. Tilgang gis.");
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

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs != null && ressurs.getArkivsaksystem() != null && ressurs.getArkivsaksnummer() != null) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
		}
		return permit();
	}
}
