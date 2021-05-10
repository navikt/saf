package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.PEP7;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_TREDJEPART;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Slf4j
@Component(PEP7)
public class Pep7Impl implements Pep<List<String>> {

	private final AbacService abacService;

	@Inject
	public Pep7Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(List<String> ressurs, SafRequestContext safRequestContext) {

		if (ressurs == null || ressurs.isEmpty()) {
			log.info("Pep7 har ingen relevante parter. Tilgang gis.");
			return XacmlResponse.permit();
		}

		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
		ressurs.forEach(aktoerId -> request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktoerId));

		Pep.traceLogPepStarted(PEP7, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP7, ressurs);

		return response;
	}
}
