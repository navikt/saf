package no.nav.saf.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import org.springframework.stereotype.Repository;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class DokumentRepositoryImpl implements DokumentRepository {

	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	public DokumentRepositoryImpl(JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public HentDokument findDokument(String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentDokument(dokumentId, variantFormat);
		} catch (Exception e) {
			log.warn("hentDokument feilet ved oppslag, dokumentId={}, variantFormat={}. Feilmelding={}",
					dokumentId, variantFormat, e.getMessage());
		}
		return null;
	}
}
