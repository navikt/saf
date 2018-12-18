package no.nav.saf.tjeneste.visningsmodell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String tittel;
	private final Journalposttype journalposttype;
	private final Journalstatus journalstatus;
	private final Tema tema;
	private final String temanavn;
	private final Sak sak;
	private final Bruker bruker;
	private final String avsenderMottakerNavn;
	private final String journalfortAvNavn;
	private final Kanal kanal;
	private final String kanalnavn;
	private final LocalDateTime datoOpprettet;
	private final List<RelevantDato> relevanteDatoer;
	private final List<DokumentInfo> dokumenter;

	@JsonCreator
	public Journalpost(@JsonProperty("journalpostId") String journalpostId,
					   @JsonProperty("tittel") String tittel,
					   @JsonProperty("journalposttype") Journalposttype journalposttype,
					   @JsonProperty("journalstatus") Journalstatus journalstatus,
					   @JsonProperty("tema") Tema tema,
					   @JsonProperty("temanavn") String temanavn,
					   @JsonProperty("sak") Sak sak,
					   @JsonProperty("bruker") Bruker bruker,
					   @JsonProperty("avsenderMottakerNavn") String avsenderMottakerNavn,
					   @JsonProperty("journalfortAvNavn") String journalfortAvNavn,
					   @JsonProperty("kanal") Kanal kanal,
					   @JsonProperty("kanalnavn") String kanalnavn,
					   @JsonProperty("datoOpprettet") LocalDateTime datoOpprettet,
					   @JsonProperty("relevantDatoer") List<RelevantDato> relevantDatoer,
					   @JsonProperty("dokumenter") List<DokumentInfo> dokumenter
	) {
		this.journalpostId = journalpostId;
		this.tittel = tittel;
		this.journalposttype = journalposttype;
		this.journalstatus = journalstatus;
		this.tema = tema;
		this.temanavn = temanavn;
		this.sak = sak;
		this.bruker = bruker;
		this.avsenderMottakerNavn = avsenderMottakerNavn;
		this.journalfortAvNavn = journalfortAvNavn;
		this.kanal = kanal;
		this.kanalnavn = kanalnavn;
		this.datoOpprettet = datoOpprettet;
		this.relevanteDatoer = relevantDatoer;
		this.dokumenter = dokumenter;
	}
}
