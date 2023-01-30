package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+farskapssaker
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP2)
public class Pep2Impl extends Pep<TilgangSak> {

	private final AbacService abacService;

	@Autowired
	public Pep2Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info("Pep2(tema-{}) mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.", ressurs.getTema().name());
			return XacmlResponse.permit();
		}

		if (isFarskapSak(ressurs) || isKontrollAnmeldelse(ressurs)) {
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA);
			request.resource(RESOURCE_FELLES_TEMA, ressurs.getTema().name());

			traceLogPepStarted(PEP2, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			traceLogPepFinished(PEP2, ressurs);

			return response;

		} else {
			return XacmlResponse.permit();
		}
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info("Pep2(tema-{}) mangler data om sak. Tilgang gis likevel for at system skal kunne knytte dokument til sak og bruker. Azure ccf.", ressurs.getTema().name());
			return permit();
		}
		if (isFarskapSak(ressurs) || isKontrollAnmeldelse(ressurs)) {
			String tema = ressurs.getTema().name().toLowerCase();
			if (isFarskapSak(ressurs)) {
				return safRequestContext.getSecurityContext().hasTemaAureRole(tema) ?
						permit() : deny(AbacAnswer.AbacDenyReason.builder()
						.cause("ingen_tilgang_farskap").policy("saf_pep2").rule("clientid_mangler_far_rolle")
						.build());
			} else {
				return safRequestContext.getSecurityContext().hasTemaAureRole(tema) ?
						permit() : deny(AbacAnswer.AbacDenyReason.builder()
						.cause("ingen_tilgang_kontroll_anmeldelse").policy("saf_pep2").rule("clientid_mangler_kta_rolle")
						.build());
			}
		} else {
			return permit();
		}
	}

	private boolean isFarskapSak(TilgangSak ressurs) {
		return FAR.equals(ressurs.getTema());
	}

	private boolean isKontrollAnmeldelse(TilgangSak ressurs) {
		return KTA.equals(ressurs.getTema());
	}

}
