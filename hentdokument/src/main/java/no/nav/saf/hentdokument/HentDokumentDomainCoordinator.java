package no.nav.saf.hentdokument;

import no.nav.saf.domain.HentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

public interface HentDokumentDomainCoordinator {

	HentDokument hentDokument(String journalpostId, String dokumentId, String variantFormat, SafRequestContext safRequestContext);
}
