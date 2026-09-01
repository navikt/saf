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
	Journalpost parent;

	String dokumentInfoId;
	String tittel;
	String brevkode;
	Dokumentstatus dokumentstatus;
	LocalDateTime datoFerdigstilt;
	String originalJournalpostId;
	Skjerming skjerming;
	boolean sensitivtPselv;
	@Builder.Default
	List<LogiskVedlegg> logiskeVedlegg = new ArrayList<>();
	@Builder.Default
	List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

	public boolean isSaksbehandlerHarTilgang() {
		return dokumentvarianter.stream()
				.filter(Dokumentvariant::isSladdetEllerArkiv)
				.anyMatch(Dokumentvariant::isSaksbehandlerHarTilgang);
	}
}
