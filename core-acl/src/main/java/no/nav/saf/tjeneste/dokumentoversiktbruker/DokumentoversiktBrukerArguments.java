package no.nav.saf.tjeneste.dokumentoversiktbruker;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerArguments {
	private final Brukeridentifikator brukeridentifikator;
	private final LocalDate fraDato;
	private final List<JournalpostType> journalposttyper;
	private final List<JournalStatus> journalstatuser;
	private final boolean visFeilregistrerte;

	public DokumentoversiktBrukerArguments(Brukeridentifikator brukeridentifikator,
										   LocalDate fraDato,
										   List<JournalpostType> journalposttyper,
										   List<JournalStatus> journalstatuser) {
		this.brukeridentifikator = brukeridentifikator;
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if (journalposttyper.isEmpty()) {
			this.journalposttyper = JournalpostType.asList();
		} else {
			this.journalposttyper = journalposttyper;
		}
		if (journalstatuser.isEmpty()) {
			this.journalstatuser = JournalStatus.mestBrukte();
		} else {
			this.journalstatuser = journalstatuser;
		}
		this.visFeilregistrerte = this.journalstatuser.contains(JournalStatus.FEILREGISTRERT);
	}
}
