package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+farskapssaker
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP2)
public class Pep2Impl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep2Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info("Pep2 mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.");
			return XacmlResponse.permit();
		}

		if (isFarskapSak(ressurs)) {
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA);
			request.resource(RESOURCE_FELLES_TEMA, FAR.name());

			Pep.traceLogPepStarted(PEP2, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			Pep.traceLogPepFinished(PEP2, ressurs);

			return response;
		} else {
			return XacmlResponse.permit();
		}
	}

	private boolean isFarskapSak(TilgangSak ressurs) {
		return FAR.equals(ressurs.getTema());
	}

}
