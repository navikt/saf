package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangGosysSak;
import no.nav.safselvbetjening.tilgang.TilgangInnsyn;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalposttype;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangPensjonSak;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalpostDto {
	private Long journalpostId;
	private Long prevJournalpostId;
	private Long nextJournalpostId;
	private Long totaltAntall;
	private String innhold;
	private FagomradeCode fagomrade;
	private String behandlingstema;
	private String behandlingstemanavn;
	private JournalStatusCode journalstatus;
	private String avsenderMottakerId;
	private AvsenderMottakerIdTypeCode avsenderMottakerIdType;
	private String avsenderMottakerNavn;
	private String avsenderMottakerLand;
	private String journalforendeEnhet;
	private String journalfortAvNavn;
	private String opprettetAvNavn;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private JournalpostTypeCode journalposttype;
	private SaksrelasjonDto saksrelasjon;
	private BrukerDto bruker;
	private Date datoOpprettet;
	private Date mottattDato;
	private Date journalDato;
	private Date dokumentDato;
	private Date avsReturDato;
	private Date sendtPrintDato;
	private Date ekspedertDato;
	private Date lestDato;
	private SkjermingTypeCode skjerming;
	private List<TilleggsopplysningDto> tilleggsopplysninger;
	private List<DokumentInfoDto> dokumenter;
	private String antallRetur;
	private String kanalReferanseId;
	private UtsendingsInfoDto utsendingsInfo;
	private String innsyn;
	private String innsynbeskrivelse;

	public boolean isTilknyttetSak() {
		return saksrelasjon != null && saksrelasjon.getSakId() != null;
	}

	public TilgangJournalpost getJournalpostTilgang(TilgangSak tilgangSak) {
		return TilgangJournalpost.builder()
				.journalstatus(TilgangJournalstatus.from(journalstatus.name()))
				.journalposttype(TilgangJournalposttype.from(journalposttype.name()))
				.mottakskanal(mottakskanal == null ? TilgangMottakskanal.IKKE_SKANNING : TilgangMottakskanal.from(mottakskanal.name()))
				.tema(fagomrade.name())
				.avsenderMottakerId(mapAvsenderMottakerId())
				.datoOpprettet(mapToLocalDateTime(datoOpprettet, LocalDateTime.MIN))
				.journalfoertDato(mapToLocalDateTime(journalDato, null))
				.skjerming(mapSkjermingType())
				.dokumenter(dokumenter == null || dokumenter.isEmpty() ? emptyList() :
						Stream.concat(
								Stream.of(dokumenter.getFirst().getTilgangDokument(true)),
								dokumenter.stream().skip(1).map(vedlegg -> vedlegg.getTilgangDokument(false))
						).toList())
				.tilgangBruker(mapTilgangBruker())
				.tilgangSak(tilgangSak)
				.innsyn(TilgangInnsyn.from(innsyn))
				.build();
	}

	private static LocalDateTime mapToLocalDateTime(Date date, LocalDateTime defaultValue) {
		if (date == null) {
			return defaultValue;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	private Ident mapAvsenderMottakerId() {
		if (avsenderMottakerId == null) {
			return null;
		}
		return Ident.ofNullable(avsenderMottakerId);
	}

	private TilgangBruker mapTilgangBruker() {
		if (bruker == null || bruker.getBrukerId() == null) {
			return null;
		}

		return new TilgangBruker(Ident.of(bruker.getBrukerId()));
	}

	private TilgangSkjermingType mapSkjermingType() {
		return TilgangSkjermingType.from(skjerming == null ? null : skjerming.name());
	}
}
