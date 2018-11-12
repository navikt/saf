package no.nav.saf.tjeneste.hentdokument;

import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

public interface HentDokumentDomainCoordinator {

	HentDokumentResponse hentDokument(final HentDokumentArguments hentDokumentArguments, final SafRequestContext safRequestContext);
}
