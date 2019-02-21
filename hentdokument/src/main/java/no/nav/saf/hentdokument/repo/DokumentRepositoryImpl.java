package no.nav.saf.hentdokument.repo;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.hentdokument.HentDokumentAntiCorruptionLayer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class DokumentRepositoryImpl implements DokumentRepository {

	private final HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer;

	@Inject
	public DokumentRepositoryImpl(HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer) {
		this.hentDokumentAntiCorruptionLayer = hentDokumentAntiCorruptionLayer;
	}

	@Override
	public HentDokument findDokument(String dokumentInfoId, String variantFormat) {
		return hentDokumentAntiCorruptionLayer.hentDokument(dokumentInfoId, variantFormat);

	}
}
