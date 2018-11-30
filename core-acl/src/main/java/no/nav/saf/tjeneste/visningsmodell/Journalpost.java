package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String tittel;
	private final Journalposttype journalposttype;
	private final Journalstatus journalstatus;
	private final Tema tema;
	private final String temanavn;
	private final Sak sak;
	private final String avsenderMottakerNavn;
	private final String journalfortAvNavn;
	private final Kanal kanal;
	private final String kanalnavn;
	@Builder.Default
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();
	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
}
