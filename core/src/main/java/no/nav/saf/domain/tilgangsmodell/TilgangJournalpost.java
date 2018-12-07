package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangJournalpost implements SecModel {
	private final String arkivsaksnummer;
	private final String arkivsaksystem;
	private final String journalpostId;
	private final String journalStatus;
	private final String journalpostType;
	private final String tema;
	private final LocalDate datoOpprettet;
	private final String mottakskanal;
	private final String avsenderMottakerId;
	@Builder.Default
	private final List<TilgangDokumentInfo> dokumenter = new ArrayList<>();
}
