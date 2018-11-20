package no.nav.saf.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	//TODO Skille mellom tekniske/fuksjonelle feil
	@Override
	public HentDokument findDokument(String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentDokument(dokumentId, variantFormat);
		} catch (Exception e) {
			log.error("hentDokument feilet ved oppslag, dokumentId={}, variantFormat={}. Feilmelding={}",
					dokumentId, variantFormat, e.getMessage());
			return HentDokument.builder()
					.dokument(getBlankPdfByteArray())
					.mediaType(MediaType.APPLICATION_PDF)
					.build();
		}
	}

	private byte[] getBlankPdfByteArray() {
		try {
			Path absoluteBlankPdfPath = Paths.get(ClassLoader.getSystemResource("blank_pdf.pdf").toURI());
			return Files.readAllBytes(absoluteBlankPdfPath);
		} catch (IOException | URISyntaxException e) {
			log.warn("Kunne ikke konvertere blank pdf til byte array");
			return new byte[0];
		}
	}
}
