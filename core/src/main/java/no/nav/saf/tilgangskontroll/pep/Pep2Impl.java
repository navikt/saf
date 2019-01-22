package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_PARAGRAF19;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+farskapssaker
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep2")
public class Pep2Impl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep2Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep2 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (hasMetadataAccess(ressurs)) {
			if (log.isTraceEnabled()) {
				log.trace("Pep2 evaluerer arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs
						.getTema());
			}
			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA);
			if (isFarskapSak(ressurs)) {
				request.resource(RESOURCE_SAF_TEMA, Tema.FAR.name());
			}
			if (isForvaltningslovensParagraf19(ressurs)) {
				request.resource(RESOURCE_SAF_PARAGRAF19, true);
			}
			XacmlResponse response = abacService.evaluate(request);
			if (log.isTraceEnabled()) {
				log.trace("Pep2 ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs
						.getTema());
			}
			return Decision.PERMIT.equals(response.getDecision());
		} else {
			return true;
		}
	}

	private boolean hasMetadataAccess(TilgangSak ressurs) {
		return isFarskapSak(ressurs) || isForvaltningslovensParagraf19(ressurs);
	}

	private boolean isFarskapSak(TilgangSak ressurs) {
		return Tema.FAR.name().equals(ressurs.getTema());
	}

	private boolean isForvaltningslovensParagraf19(TilgangSak ressurs) {
		return ressurs.isParagraf19();
	}
}
