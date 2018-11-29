package no.nav.saf.tjeneste.dokumentoversiktbruker;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalz;
import no.nav.saf.tjeneste.visningsmodell.kode.Journazz;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerArguments {
	private final Brukeridentifikator brukeridentifikator;
	private final LocalDate fraDato;
	private final List<Journalz> journalposttyper;
	private final List<Journazz> journalstatuser;
	private final boolean visFeilregistrerte;

	public DokumentoversiktBrukerArguments(Brukeridentifikator brukeridentifikator,
										   LocalDate fraDato,
										   List<Journalz> journalposttyper,
										   List<Journazz> journalstatuser) {
		this.brukeridentifikator = brukeridentifikator;
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if (journalposttyper.isEmpty()) {
			this.journalposttyper = Journalz.asList();
		} else {
			this.journalposttyper = journalposttyper;
		}
		if (journalstatuser.isEmpty()) {
			this.journalstatuser = Journazz.asList();
		} else {
			this.journalstatuser = journalstatuser;
		}
		this.visFeilregistrerte = this.journalstatuser.contains(Journazz.FEILREGISTRERT);
	}
}
