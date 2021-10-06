package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_PERSON;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/FP1%3A+Behandling+Kode+6+Brukere
 * https://confluence.adeo.no/display/ABAC/FP2%3A+Behandling+Kode+7+Brukere
 * https://confluence.adeo.no/display/ABAC/FP3%3A+Egen+ansatt
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component(PEP1G)
@Slf4j
public class Pep1gImpl extends Pep<TilgangBruker> {

	private final AbacService abacService;

	@Inject
	public Pep1gImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.isUkjent()) {
			log.info("Pep1g mangler data om bruker. Tilgang gis for å kunne identifisere bruker.");
			return XacmlResponse.permit();
		} else if (ressurs.isOrganisasjon()) {
			log.info("Pep1g validerer organisasjon. Tilgang gis siden bruker er en organisasjon.");
			return XacmlResponse.permit();
		}

		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON);

		if (ressurs.getAktoerId() != null) {
			request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, ressurs.getAktoerId());
		} else if (ressurs.getFoedselsnr() != null) {
			request.resource(RESOURCE_FELLES_PERSON_FNR, ressurs.getFoedselsnr());
		} else {
			String message = "Pep1g kunne ikke validere bruker fordi bruker ikke er en person. Denne tilstanden indikerer en teknisk feil.";
			log.error(message);
			return XacmlResponse.deny();
		}
		traceLogPepStarted(PEP1G, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		traceLogPepFinished(PEP1G, ressurs);
		return response;
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		return permit();
	}
}
