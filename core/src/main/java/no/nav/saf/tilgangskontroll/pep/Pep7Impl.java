package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static no.nav.saf.domain.DomainConstants.PEP7;
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
@Component(PEP7)
public class Pep7Impl extends Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep7Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	private final List<Tema> relevanteTemaK9 = Arrays.asList(FRI, OMS);

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs != null) {
			if (FOR.equals(ressurs.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(ressurs.getFagsaksystem())) {
				if (ressurs.getFpAktoerIdList() == null) {
					return XacmlResponse.deny();
				}
				if (ressurs.getFpAktoerIdList().isEmpty()) {
					log.info("Pep7 har ingen relevante parter. Tilgang gis.");
					return XacmlResponse.permit();
				}
				return getXacmlResponse(ressurs, safRequestContext, ressurs.getFpAktoerIdList());
			}

			if (relevanteTemaK9.contains(ressurs.getTema()) && FAGSAKSYSTEM_K9.equals(ressurs.getFagsaksystem())) {
				if (ressurs.getK9AktoerIdList() == null) {
					return XacmlResponse.deny();
				}
				if (ressurs.getK9AktoerIdList().isEmpty()) {
					log.info("Pep7 har ingen relevante parter. Tilgang gis.");
					return XacmlResponse.permit();
				}
				return getXacmlResponse(ressurs, safRequestContext, ressurs.getK9AktoerIdList());
			}
		}
		return XacmlResponse.permit();
	}

	private XacmlResponse getXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext, List<String> aktoerIdList) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());

		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
		aktoerIdList.forEach(aktoerId -> request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktoerId));

		traceLogPepStarted(PEP7, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP7, ressurs);

		return response;
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		return permit();
	}
}
