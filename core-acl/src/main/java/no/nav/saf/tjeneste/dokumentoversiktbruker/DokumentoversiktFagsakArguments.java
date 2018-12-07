package no.nav.saf.tjeneste.dokumentoversiktbruker;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktFagsakArguments {

	private final String fagsakId;
	private final String fagsaksystem;
	private final LocalDate fraDato;
	private final List<Tema> tema;
	private final List<Journalposttype> journalposttyper;
	private final List<Journalstatus> journalstatuser;
	private final boolean visFeilregistrerte;
	private final Integer foerste;
	private final String etterPeker;
	private final Integer siste;
	private final String foerPeker;

	public DokumentoversiktFagsakArguments(String fagsakId,
										   String fagsaksystem,
										   LocalDate fraDato,
										   List<Tema> tema,
										   List<Journalposttype> journalposttyper,
										   List<Journalstatus> journalstatuser,
										   Integer foerste,
										   String etterPeker,
										   Integer siste,
										   String foerPeker) {
		this.fagsakId = fagsakId;
		this.fagsaksystem = fagsaksystem;
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if (tema.isEmpty()) {
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
		this.foerste = foerste;
		this.etterPeker = etterPeker;
		this.siste = siste;
		this.foerPeker = foerPeker;
	}
}
