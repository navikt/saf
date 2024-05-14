package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Skjerming;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class TilgangJournalpost {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	Journalstatus journalstatus;
	Skjerming skjerming;
	TilgangSak tilgangSak;
	String journalpostTittel;
	@Builder.Default
	List<TilgangDokumentInfo> dokumenter = new ArrayList<>();

	/**
	 * Attributt brukt for å forenkle kodeflyt
	 **/
	String journalpostId;

}
