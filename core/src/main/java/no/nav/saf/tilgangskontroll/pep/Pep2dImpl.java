package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static no.nav.saf.domain.DomainConstants.SAF;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep2d")
public class Pep2dImpl implements Pep<TilgangSak> {

	private final AbacService abacService;

	@Inject
	public Pep2dImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep2d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		XacmlRequest request = new XacmlRequest();

		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, safRequestContext.getSecurityContext().getOidcTokenBody());
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);

		if (ressurs.getTema() != null) {
			if (log.isTraceEnabled()) {
				log.trace("Pep2d evaluerer arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs.getTema());
			}
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TEMA);
			request.resource(RESOURCE_SAF_TEMA, ressurs.getTema());
			XacmlResponse response = abacService.evaluate(request);
			// TODO distributed cache
			if (log.isTraceEnabled()) {
				log.trace("Pep2d ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs.getTema());
			}
			return Decision.PERMIT.equals(response.getDecision());
		} else {
			return true;
		}
	}
}
