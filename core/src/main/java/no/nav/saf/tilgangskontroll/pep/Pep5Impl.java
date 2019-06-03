package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.domain.DomainConstants.PEP5;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
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
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
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
	public XacmlResponse verifyAccessXacmlResponse(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep5 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return XacmlResponse.deny();
		}

		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
		if (isSkjermingPresent(ressurs)) {
			XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
			boolean decide = decide(response.getDecision());
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide);
			return response;
		} else {
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
			return XacmlResponse.permit();
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private XacmlResponse hasDokumentAccess(TilgangDokumentInfo ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_METADATA);
		request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());

		Pep.traceLogPepStarted(PEP5, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP5, ressurs);

		return response;
	}

	private boolean isSkjermingPresent(TilgangDokumentInfo ressurs) {
		return ressurs.getSkjerming() != null;
	}
}
