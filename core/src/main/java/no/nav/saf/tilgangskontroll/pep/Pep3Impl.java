package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TREDJEPART;
import static no.nav.saf.domain.DomainConstants.PEP3;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+paragraf19
 *
 * @author Joakim Bjørnstad, Jbit AS
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
		if (ressurs == null || ressurs.getRelevanteTredjeparter() == null) {
			log.warn("Pep3 mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.");
			return XacmlResponse.permit();
		}

		if (hasMetadataAccess(ressurs)) {
			if (hasNotRelevanteTredjeparter(ressurs)) {
				return XacmlResponse.permit();
			}
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART);
			ressurs.getRelevanteTredjeparter()
					.forEach(tilgangRelevantTredjepart -> request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangRelevantTredjepart
							.getIdent().getIdentifikator()));

			Pep.traceLogPepStarted(PEP3, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			Pep.traceLogPepFinished(PEP3, ressurs);

			return response;
		} else {
			return XacmlResponse.permit();
		}
	}

	private boolean hasNotRelevanteTredjeparter(TilgangSak ressurs) {
		return ressurs.getRelevanteTredjeparter().isEmpty();
	}

	private boolean hasMetadataAccess(TilgangSak ressurs) {
		return isFarskapSak(ressurs) || isBidragSak(ressurs);
	}

	private boolean isFarskapSak(TilgangSak ressurs) {
		return Tema.FAR.equals(ressurs.getTema());
	}

	private boolean isBidragSak(TilgangSak ressurs) {
		return Tema.BID.equals(ressurs.getTema());
	}
}
