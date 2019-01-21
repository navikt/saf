package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNALSTATUS;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNAL_METADATA;
import static no.nav.saf.domain.DomainConstants.ABAC_JOURNALSTATUS_UTGAAR;

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
@Component("pep4")
public class Pep4Impl implements Pep<TilgangJournalpost> {

	private final AbacService abacService;

	@Inject
	public Pep4Impl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep4 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (isJournalpoststatusUtgaar(ressurs)) {
			return hasJournalpostAccess(safRequestContext);
		}
		return true;
	}

	private boolean hasJournalpostAccess(SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA);
		request.resource(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR);
		XacmlResponse response = abacService.evaluate(request);
		return Decision.PERMIT.equals(response.getDecision());
	}

	private boolean isJournalpoststatusUtgaar(TilgangJournalpost ressurs) {
		return Journalstatus.UTGAAR.equals(ressurs.getJournalstatus());
	}
}
