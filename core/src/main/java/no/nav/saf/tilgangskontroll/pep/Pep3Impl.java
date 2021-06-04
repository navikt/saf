package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.kode.Tema.BID;
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
@Component(PEP3)
public class Pep3Impl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep3Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext) {

		if (ressurs == null || ressurs.getRelevanteTredjeparter() == null || ressurs.getRelevanteTredjeparter().isEmpty()) {
			log.info("Pep3 har ingen relevante parter. Tilgang gis.");
			return XacmlResponse.permit();
		}

		if (BID.equals(ressurs.getTema()) && FAGSAKSYSTEM_BISYS.equals(ressurs.getFagsaksystem())) {
			List<TilgangRelevantTredjepart> relevantTredjeparter = ressurs.getRelevanteTredjeparter();

			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());

			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
			relevantTredjeparter.forEach(tilgangRelevantTredjepart -> {
				request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangRelevantTredjepart.getIdent().getIdentifikator());
			});

			Pep.traceLogPepStarted(PEP3, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			Pep.traceLogPepFinished(PEP3, ressurs);

			return response;
		}
		return XacmlResponse.permit();
	}
}
