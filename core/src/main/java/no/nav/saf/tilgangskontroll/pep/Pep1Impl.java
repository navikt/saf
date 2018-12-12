package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.domain.DomainConstants.SAF;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_JOURNALPOST;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementerer PEP1:
 * <p>
 * FP1 Behandling av Kode 6 Brukere
 * FP2 Behandling av Kode 7 Brukere
 * FP3 Egen ansatt
 * Saksbehandlers tilgang til enhet
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("pep1")
@Slf4j
public class Pep1Impl implements Pep<TilgangBruker> {

	private final AbacService abacService;

	@Inject
	public Pep1Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep1 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		} else if (ressurs.getOrgnummer() != null) {
			return true;
		}

		XacmlRequest request = new XacmlRequest();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, safRequestContext.getSecurityContext().getOidcTokenBody());
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNALPOST);

		if (ressurs.getAktoerId() != null) {
			request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, ressurs.getAktoerId());
		} else if (ressurs.getFoedselsnr() != null) {
			request.resource(RESOURCE_FELLES_PERSON_FNR, ressurs.getFoedselsnr());
		} else {
			return false;
		}

		XacmlResponse response = abacService.evaluate(request);

		return Decision.PERMIT.equals(response.getDecision());
	}

}
