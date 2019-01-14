package no.nav.saf.hentdokument.repo;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.domain.HentDokument;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class DokumentRepositoryImpl implements DokumentRepository {

	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public DokumentRepositoryImpl(JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public HentDokument findDokument(String dokumentInfoId, String variantFormat) {
		return joarkAntiCorruptionLayer.hentDokument(dokumentInfoId, variantFormat);

	}
}
