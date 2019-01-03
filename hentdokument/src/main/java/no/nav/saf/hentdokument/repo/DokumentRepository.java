package no.nav.saf.hentdokument.repo;

import no.nav.saf.domain.HentDokument;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface DokumentRepository {

	HentDokument findDokument(String dokumentId, String variantFormat);

}
