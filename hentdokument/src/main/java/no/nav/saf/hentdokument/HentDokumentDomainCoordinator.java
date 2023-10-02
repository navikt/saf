package no.nav.saf.hentdokument;

import no.nav.saf.domain.HentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;

public interface HentDokumentDomainCoordinator {
	HentDokument hentDokument(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContext);
}
