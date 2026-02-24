package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Skjerming;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Value
@ToString(exclude = "parent")
@Builder
public class DokumentInfo {
	private final Journalpost parent;

	private final String dokumentInfoId;
	private final String tittel;
	private final String brevkode;
	private final Dokumentstatus dokumentstatus;
	private final LocalDateTime datoFerdigstilt;
	private final String originalJournalpostId;
	private final Skjerming skjerming;
	private final Boolean sensitivtPselv;
	@Builder.Default
	private final List<LogiskVedlegg> logiskeVedlegg = new ArrayList<>();
	@Builder.Default
	private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
}
