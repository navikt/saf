package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNALSTATUS;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNAL_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.domain.DomainConstants.ABAC_JOURNALSTATUS_UTGAAR;
import static no.nav.saf.domain.DomainConstants.PEP4;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
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
 * https://confluence.adeo.no/display/ABAC/Journalpoststatus
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP4)
public class Pep4Impl implements Pep<TilgangJournalpost> {

	private final AbacService abacService;

	@Inject
	public Pep4Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep4 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return XacmlResponse.deny();
		}

		if (isJournalpoststatusUtgaar(ressurs) || isSkjermingPresent(ressurs)) {
			return hasJournalpostAccess(safRequestContext, ressurs);
		} else {
			return XacmlResponse.permit();
		}
	}

	@Override
	public boolean hasAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		XacmlResponse response = verifyAccessXacmlResponse(ressurs, safRequestContext);
		return Decision.PERMIT.equals(response.getDecision());
	}

	private XacmlResponse hasJournalpostAccess(SafRequestContext safRequestContext, TilgangJournalpost ressurs) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA);

		if (isJournalpoststatusUtgaar(ressurs)) {
			request.resource(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR);
		}
		if (isSkjermingPresent(ressurs)) {
			request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());
		}

		Pep.traceLogPepStarted(PEP4, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP4, ressurs);

		return response;
	}

	private boolean isJournalpoststatusUtgaar(TilgangJournalpost ressurs) {
		return Journalstatus.UTGAAR.equals(ressurs.getJournalstatus());
	}

	private boolean isSkjermingPresent(TilgangJournalpost ressurs) {
		return ressurs.getSkjerming() != null;
	}

}
