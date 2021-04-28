package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.PEPX;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_PERSON;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Component(PEPX)
@Slf4j
public class PepXimpl implements Pep<List<String>>{

	private final AbacService abacService;

	@Inject
	public PepXimpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(List<String> ressurs, SafRequestContext safRequestContext) {

		List<XacmlResponse> responseList = new ArrayList<>();
		Pep.traceLogPepStarted(PEPX, ressurs);

		for(String aktoerId : ressurs){
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON); //todo?
			request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktoerId);
			responseList.add(abacService.evaluate(request));
		}
		Pep.traceLogPepFinished(PEPX, ressurs);

		return responseList.contains(XacmlResponse.deny()) ? XacmlResponse.deny() : XacmlResponse.permit();
	}
}
