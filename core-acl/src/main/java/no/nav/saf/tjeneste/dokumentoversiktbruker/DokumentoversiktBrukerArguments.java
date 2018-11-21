package no.nav.saf.tjeneste.dokumentoversiktbruker;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerArguments {
	private final String aktoerId;
	private final LocalDate fraDato;
	private final List<JournalpostType> journalposttyper;
	private final List<JournalStatus> journalstatuser;
	private final boolean visFeilregistrerte;

	public DokumentoversiktBrukerArguments(String aktoerId, LocalDate fraDato, List<JournalpostType> journalposttyper, List<JournalStatus> journalstatuser) {
		this.aktoerId = aktoerId;
		if(fraDato == null) {
			this.fraDato = LocalDate.now().minusMonths(12);
		} else {
			this.fraDato = fraDato;
		}
		if(journalposttyper.isEmpty()) {
			this.journalposttyper = JournalpostType.asList();
		} else {
			this.journalposttyper = journalposttyper;
		}
		if(journalstatuser.isEmpty()) {
			this.journalstatuser = JournalStatus.asList();
		} else {
			this.journalstatuser = journalstatuser;
		}
		this.visFeilregistrerte = this.journalstatuser.contains(JournalStatus.FEILREGISTRERT);
	}
}
