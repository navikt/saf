package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.domain.DomainConstants.PEP5;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Dekker følgende policies i saf:
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP5)
public class Pep5Impl implements Pep<TilgangDokumentInfo> {

	private final AbacService abacService;

	@Inject
	public Pep5Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (isSkjermingPresent(ressurs)) {
			return hasDokumentAccess(safRequestContext, ressurs);
		} else {
			return true;
		}
	}

	private boolean hasDokumentAccess(SafRequestContext safRequestContext, TilgangDokumentInfo ressurs) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_METADATA);
		request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());

		Pep.traceLogPepStarted(PEP5, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP5, ressurs);

		return Decision.PERMIT.equals(response.getDecision());
	}

	private boolean isSkjermingPresent(TilgangDokumentInfo ressurs) {
		return ressurs.getSkjerming() != null;
	}
}
