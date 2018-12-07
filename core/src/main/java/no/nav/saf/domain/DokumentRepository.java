package no.nav.saf.domain;

import no.nav.saf.tjeneste.hentdokument.HentDokument;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface DokumentRepository {

	HentDokument findDokument(String dokumentId, String variantFormat);

}
