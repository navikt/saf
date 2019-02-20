package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDateTime;
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
	private final String behandlingstema;
	private final String behandlingstemanavn;
	private final Sak sak;
	private final Bruker bruker;
	private final String avsenderMottakerNavn;
	private final String avsenderMottakerLand;
	private final String journalforendeEnhet;
	private final String journalfortAvNavn;
	private final String opprettetAvNavn;
	private final Kanal kanal;
	private final String kanalnavn;
	private final LocalDateTime datoOpprettet;
	@Builder.Default
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();
	@Builder.Default
	private final List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();
	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
}
