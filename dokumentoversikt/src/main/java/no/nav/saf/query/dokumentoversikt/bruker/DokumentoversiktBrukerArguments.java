package no.nav.saf.query.dokumentoversikt.bruker;

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
	private final Integer foerste;
	private final String etterPeker;
	private final Integer siste;
	private final String foerPeker;

	public DokumentoversiktBrukerArguments(BrukerIdInput brukerIdInput,
										   LocalDate fraDato,
										   List<Tema> tema,
										   List<Journalposttype> journalposttyper,
										   List<Journalstatus> journalstatuser,
										   Integer foerste,
										   String etterPeker,
										   Integer siste,
										   String foerPeker) {
		this.brukerIdInput = brukerIdInput;
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if(tema.isEmpty()) {
			this.tema = Tema.ALL;
		} else {
			this.tema = tema;
		}
		if (journalposttyper.isEmpty()) {
			this.journalposttyper = Journalposttype.ALL;
		} else {
			this.journalposttyper = journalposttyper;
		}
		if (journalstatuser.isEmpty()) {
			this.journalstatuser = Journalstatus.ALL;
		} else {
			this.journalstatuser = journalstatuser;
		}
		this.visFeilregistrerte = this.journalstatuser.contains(Journalstatus.FEILREGISTRERT);
		this.foerste = foerste;
		this.etterPeker = etterPeker;
		this.siste = siste;
		this.foerPeker = foerPeker;
	}
}
