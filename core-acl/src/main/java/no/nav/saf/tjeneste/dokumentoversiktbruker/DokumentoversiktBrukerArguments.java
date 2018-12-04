package no.nav.saf.tjeneste.dokumentoversiktbruker;

import lombok.Value;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerArguments {
	private final BrukerIdInput brukerIdInput;
	private final LocalDate fraDato;
	private final List<Tema> tema;
	private final List<Journalposttype> journalposttyper;
	private final List<Journalstatus> journalstatuser;
	private final boolean visFeilregistrerte;

	public DokumentoversiktBrukerArguments(BrukerIdInput brukerIdInput,
										   LocalDate fraDato,
										   List<Tema> tema,
										   List<Journalposttype> journalposttyper,
										   List<Journalstatus> journalstatuser) {
		this.brukerIdInput = brukerIdInput;
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if(tema.isEmpty()) {
			this.tema = Tema.asList();
		} else {
			this.tema = tema;
		}
		if (journalposttyper.isEmpty()) {
			this.journalposttyper = Journalposttype.asList();
		} else {
			this.journalposttyper = journalposttyper;
		}
		if (journalstatuser.isEmpty()) {
			this.journalstatuser = Journalstatus.asList();
		} else {
			this.journalstatuser = journalstatuser;
		}
		this.visFeilregistrerte = this.journalstatuser.contains(Journalstatus.FEILREGISTRERT);
	}
}
