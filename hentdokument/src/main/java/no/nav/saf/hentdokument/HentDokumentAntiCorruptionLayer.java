package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.hentdokument.HentDokumentResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.HentDokument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.nav.saf.util.MimetypeFileextensionMapper.toFileextension;

@Slf4j
@Component
public class HentDokumentAntiCorruptionLayer {
	public static final Set<String> HENTDOKUMENT_TILGANG_FIELDS = Set.of("journalpostId", "fagomraade", "status", "skjerming", "bruker", "saksrelasjon", "dokumenter.dokumentInfoId", "dokumenter.skjerming", "dokumenter.fildetaljer", "innhold");

	private final DokarkivConsumer dokarkivConsumer;

	@Autowired
	public HentDokumentAntiCorruptionLayer(DokarkivConsumer dokarkivConsumer) {
		this.dokarkivConsumer = dokarkivConsumer;
	}

	public ArkivJournalpost hentDokumentTilgang(String journalpostId, String dokumentInfoId) {
		return dokarkivConsumer.journalpostByIdAndDokumentInfoId(journalpostId, dokumentInfoId, HENTDOKUMENT_TILGANG_FIELDS);
	}

	public HentDokument hentDokument(String dokumentInfoId, String variantFormat) {
		HentDokumentResponseTo responseTo = dokarkivConsumer.hentDokument(dokumentInfoId, variantFormat);

		return HentDokument.builder()
				.dokument(responseTo.dokument())
				.mediaType(responseTo.mediaType())
				.extension(toFileextension(responseTo.mediaType()))
				.build();
	}
}
