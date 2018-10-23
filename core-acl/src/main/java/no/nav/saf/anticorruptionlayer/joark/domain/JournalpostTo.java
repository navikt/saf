package no.nav.saf.anticorruptionlayer.joark.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FaktiskDistribusjonskanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostTo {

	private Long journalpostId;
	private String journalForendeEnhetId;
	private Date journalDato;
	private Date sendtPrintDato;
	private Integer antallRetur;
	private Date avsendtReturDato;
	private String innhold;
	private String kravtype;
	private String merknad;
	private String fordeling;
	private Boolean originaltBestilt;
	private String kanalReferanseId;
	private FagomradeCode fagomrade;
	private JournalStatusCode journalstatus;
	private Date dokumentDato;
	private String avsenderMottaker;
	private String avsenderMottakerId;
	private String journalfortAvNavn;
	private Date mottattDato;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private String land;
	private FaktiskDistribusjonskanalCode faktiskDistribusjonskanal;
	private Boolean elektroniskDistribusjon;
	private Date ekspedertDato;
	private Date lestDato;
	private Date mottattAdressatDato;
	private JournalpostTypeCode journalposttype;
	private Boolean signatur;
	private SaksrelasjonTo saksrelasjon;
	private Date datoOpprettet;
	@Builder.Default
	private final Set<JournalpostDokumentInfoRelasjonTo> journalpostDokumentInfoRelasjoner = new HashSet<>();
//	private final Set<Kryssreferanse> kryssreferanser = new HashSet<>(); TODO Trenger vi denne?
//	private final Set<ReturInfo> returInfos = new HashSet<>(); TODO Trenger vi denne?
//	private Behandlingsrelasjon behandlingsrelasjon; TODO Trenger vi denne?

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SaksrelasjonTo {
		private Long saksrelasjonId;
		private String sakId;
		private Boolean feilregistrert;
		private String endretAvNavn;
		private FagsystemCode fagsystem;
	}

}
