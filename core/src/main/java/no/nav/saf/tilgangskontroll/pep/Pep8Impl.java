package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.PEP8;
import static no.nav.saf.domain.tilgangsmodell.IdentType.AKTOERID;
import static no.nav.saf.domain.tilgangsmodell.IdentType.FOLKEREGISTERIDENT;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_TREDJEPART;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 */
@Slf4j
@Component(PEP8)
public class Pep8Impl implements Pep<List<TilgangRelevantTredjepart>> {

	private final AbacService abacService;

	@Inject
	public Pep8Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(List<TilgangRelevantTredjepart> ressurs, SafRequestContext safRequestContext) {

		if (ressurs == null || ressurs.isEmpty()) {
			log.info("Pep8 har ingen relevante parter. Tilgang gis.");
			return XacmlResponse.permit();
		}

		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());

		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
		ressurs.forEach(tilgangRelevantTredjepart -> {
			if (FOLKEREGISTERIDENT.equals(tilgangRelevantTredjepart.getIdent().getIdentType())) {
				request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangRelevantTredjepart.getIdent().getIdentifikator());
			} else if(AKTOERID.equals(tilgangRelevantTredjepart.getIdent().getIdentType())){
				request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, tilgangRelevantTredjepart.getIdent().getIdentifikator());
			}
		});

		Pep.traceLogPepStarted(PEP8, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP8, ressurs);

		return response;
	}
}
