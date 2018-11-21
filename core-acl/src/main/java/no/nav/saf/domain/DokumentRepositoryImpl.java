package no.nav.saf.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
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
	public HentDokument findDokument(String dokumentId, String variantFormat) {
		return joarkAntiCorruptionLayer.hentDokument(dokumentId, variantFormat);

	}
}
