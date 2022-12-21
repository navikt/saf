package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Skjerming;
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
	String journalpostId;
	String tittel;
	Journalposttype journalposttype;
	Journalstatus journalstatus;
	Tema tema;
	String temanavn;
	String behandlingstema;
	String behandlingstemanavn;
	Sak sak;
	Bruker bruker;
	AvsenderMottaker avsenderMottaker;
	Utsendingsinfo utsendingsinfo;
	/**
	 * @since 4.5.0
	 * @deprecated Konsumenter bes bruke {@code AvsenderMottaker.id} i stedet. Feltet overvåkes for bruk og vil bli fjernet i fremtiden.
	 */
	@Deprecated(since = "4.5.0", forRemoval = true)
	String avsenderMottakerId;
	/**
	 * @since 4.5.0
	 * @deprecated Konsumenter bes bruke {@code AvsenderMottaker.navn} i stedet. Feltet overvåkes for bruk og vil bli fjernet i fremtiden.
	 */
	@Deprecated(since = "4.5.0", forRemoval = true)
	String avsenderMottakerNavn;
	/**
	 * @since 4.5.0
	 * @deprecated Konsumenter bes bruke {@code AvsenderMottaker.land} i stedet. Feltet overvåkes for bruk og vil bli fjernet i fremtiden.
	 */
	@Deprecated(since = "4.5.0", forRemoval = true)
	String avsenderMottakerLand;
	/**
	 * @since 6.2.0
	 * @deprecated Konsumenter bes bruke {@code journalfoerendeEnhet} i stedet. Feltet overvåkes for bruk og vil bli fjernet i fremtiden.
	 */
	@Deprecated(since = "6.2.0", forRemoval = true)
	String journalforendeEnhet;
	String journalfoerendeEnhet;
	String journalfortAvNavn;
	String opprettetAvNavn;
	Kanal kanal;
	String kanalnavn;
	Skjerming skjerming;
	LocalDateTime datoOpprettet;
	@Builder.Default
	List<RelevantDato> relevanteDatoer = new ArrayList<>();
	String antallRetur;
	String eksternReferanseId;
	@Builder.Default
	List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();
	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();
}
