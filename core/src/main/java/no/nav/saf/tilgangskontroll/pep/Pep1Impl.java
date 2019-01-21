package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_PERSON;

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
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 * https://confluence.adeo.no/display/ABAC/FP3%3A+Egen+ansatt
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

		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON);

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
