package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivAvsenderMottaker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDokumentinfo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivLogiskVedlegg;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivRelevanteDatoer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivUtsendingsInfo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArkivJournalpostTestObjects {
	static final VariantFormatCode VARIANT_FORMAT_CODE_ARKIV = VariantFormatCode.ARKIV;
	static final VariantFormatCode VARIANT_FORMAT_CODE_SLADDET = VariantFormatCode.SLADDET;
	static final SkjermingTypeCode SKJERMING_TYPE_CODE_POL = SkjermingTypeCode.POL;
	static final String ARKIVDOKUMENTINFO_BREVKODE = "NAV 10-07.34";
	static final long ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID = 454231103;
	static final long ARKIVJOURNALPOST_JOURNALPOST_ID = 453842483;
	static final String ARKIVJOURNALPOST_INNHOLD = "NAV 10-07.34 Tilskudd ved kjøp av briller til barn";
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_OPPRETTET = OffsetDateTime.parse("2023-11-16T11:10:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_AVS_RETUR_DATO = OffsetDateTime.parse("2023-11-16T11:11:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_SENDT_PRINT_DATO = OffsetDateTime.parse("2023-11-16T11:12:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_EKSPEDERT_DATO = OffsetDateTime.parse("2023-11-16T11:13:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_DOKUMENT_DATO = OffsetDateTime.parse("2023-11-16T11:14:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_JOURNAL_DATO = OffsetDateTime.parse("2023-11-16T11:15:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_MOTTAT_DATO = OffsetDateTime.parse("2023-11-16T11:16:00.292+00:00");
	static final OffsetDateTime ARKIVDOKUMENT_INFO_DATO_FERDIGSTILT = OffsetDateTime.parse("2023-11-16T11:17:00.292+00:00");
	static final OffsetDateTime ARKIVJOURNALPOST_LEST_DATO = OffsetDateTime.parse("2023-11-16T11:18:00.292+00:00");
	static final String FNR = "07480900000";
	static final Long ARKIVSAKSRELASJON_SAK_ID = 140000000L;
	static final FagsystemCode ARKIVSAKRELASJON_FAGSYSTEM = FagsystemCode.FS22;
	static final String ARKIVSAK_AKTOERID = "2321000000000";
	static final String ARKIVSAK_FAGSAKNR = "2514";
	static final String ARKIVSAK_APPLIKASJON = "HJELPEMIDLER";
	static final String ARKIVSAK_ORGNR = "999999999";
	static final LocalDateTime ARKIVSAK_OPPRETTET_TIDSPUNKT = LocalDateTime.parse("2023-11-16T11:19:00.292");
	static final FagomradeCode ARKIVJOURNALPOST_FAGOMRAADE = FagomradeCode.HJE;
	static final String ARKIVJOURNALPOST_JOURNALFOERT_AV = "teamdigihot:hm-joark-sink";
	static final String ARKIVJOURNALPOST_BEHANDLINGSTEMA = "ab0072";
	static final String ARKIVJOURNALPOST_BEHANDLINGSTEMANAVN = "Foreldrepenger ved adopsjon";
	static final String AVSENDER_MOTTAKER_ID = "07480900000";
	static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE_CODE = AvsenderMottakerIdTypeCode.FNR;
	static final String AVSENDER_MOTTAKER_NAVN = "Max Mekker";
	static final String AVSENDER_MOTTAKER_LAND = "NO";
	static final String ARKIVJOURNALPOST_JOURNALFOERENDE_ENHET = "4710";
	static final String ARKIVJOURNALPOST_OPPRETTET_AV_NAVN = "Bjarne Betjent";
	static final String TILLEGGSOPPLYSNING_NOKKEL = "bucid";
	static final String TILLEGGSOPPLYSNING_VERDI = "21521";
	static final String ARKIVFILDETALJER_FILNAVN_1 = "filnavn1";
	static final String ARKIVFILDETALJER_FILNAVN_2 = "filnavn2";
	static final Long LOGISK_VEDLEGG_ID = 30000L;
	static final String LOGISK_VEDLEGG_TITTEL = "logisktittel";
	static final String ARKIVDOKUMENTINFO_DOKUMENTTYPE_ID = "00000001";
	static final String ARKIVFILDETALJER_FILUUID_1 = "4b4d0d13-5c8c-4f6b-922c-4026f1679000";
	static final String ARKIVFILDETALJER_FILUUID_2 = "4b4d0d13-5c8c-4f6b-922c-4026f1679001";
	static final String BRUKER_ID_PERSON = "07480900000";
	static final String BRUKER_ID_ORGANISASJON = "999999999";
	static final Integer ARKIVJOURNALPOST_ANTALL_RETUR = 3;
	static final String ARKIVJOURNALPOST_KANALREFERANSE_ID = "00000000-5c8c-4f6b-922c-4026f1679000";
	static final String FILTYPE_1 = "PDFA";
	static final String FILTYPE_2 = "PDF";
	static final String ARKIVDOKUMENTINFO_TITTEL = "NAV 10-07.34 Tilskudd ved kjøp av briller til barn";


	static ArkivJournalpost utgaaendeArkivJournalpost(JournalStatusCode journalStatusCode, ArkivUtsendingsInfo arkivUtsendingsInfo, UtsendingsKanalCode kanalCode) {
		return utgaaendeArkivJournalpostBuilder(journalStatusCode, arkivUtsendingsInfo, kanalCode).build();
	}

	static ArkivJournalpost.ArkivJournalpostBuilder utgaaendeArkivJournalpostBuilder(JournalStatusCode journalStatusCode, ArkivUtsendingsInfo arkivUtsendingsInfo, UtsendingsKanalCode kanalCode) {
		return baseArkivJournalpost()
				.type(JournalpostTypeCode.U.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), false,
						new ArkivSak(ARKIVJOURNALPOST_FAGOMRAADE.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.status(journalStatusCode.name())
				.relevanteDatoer(ArkivRelevanteDatoer.builder()
						.opprettet(ARKIVJOURNALPOST_DATO_OPPRETTET)
						.journalfoert(ARKIVJOURNALPOST_JOURNAL_DATO)
						.hoveddokument(ARKIVJOURNALPOST_DOKUMENT_DATO)
						.lest(ARKIVJOURNALPOST_LEST_DATO)
						.retur(ARKIVJOURNALPOST_AVS_RETUR_DATO)
						.sendtPrint(ARKIVJOURNALPOST_SENDT_PRINT_DATO)
						.ekspedert(ARKIVJOURNALPOST_EKSPEDERT_DATO)
						.build())
				.antallRetur(ARKIVJOURNALPOST_ANTALL_RETUR)
				.utsendingskanal(kanalCode == null ? null : kanalCode.name())
				.utsendingsInfo(arkivUtsendingsInfo);
	}

	static ArkivJournalpost inngaaendeArkivJournalpost() {
		return inngaaendeArkivJournalpostBuilder().build();
	}

	static ArkivJournalpost.ArkivJournalpostBuilder inngaaendeArkivJournalpostBuilder() {
		return baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), false,
						new ArkivSak(ARKIVJOURNALPOST_FAGOMRAADE.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.relevanteDatoer(ArkivRelevanteDatoer.builder()
						.opprettet(ARKIVJOURNALPOST_DATO_OPPRETTET)
						.forsendelseMottatt(ARKIVJOURNALPOST_MOTTAT_DATO)
						.journalfoert(ARKIVJOURNALPOST_JOURNAL_DATO)
						.build())
				.mottakskanal(MottaksKanalCode.NAV_NO.name());
	}

	static ArkivJournalpost notatArkivJournalpost() {
		return baseArkivJournalpost()
				.type(JournalpostTypeCode.N.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), false,
						new ArkivSak(ARKIVJOURNALPOST_FAGOMRAADE.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.relevanteDatoer(ArkivRelevanteDatoer.builder()
						.opprettet(ARKIVJOURNALPOST_DATO_OPPRETTET)
						.journalfoert(ARKIVJOURNALPOST_JOURNAL_DATO)
						.build())
				.build();
	}

	static ArkivJournalpost pensjonSakArkivJournalpost() {
		return baseArkivJournalpost()
				.type(JournalpostTypeCode.N.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, FagsystemCode.PEN.name(), false,
						new ArkivSak(null, null, null, null, null, null)))
				.build();
	}

	static ArkivJournalpost.ArkivJournalpostBuilder baseArkivJournalpost() {
		return ArkivJournalpost.builder()
				.journalpostId(ARKIVJOURNALPOST_JOURNALPOST_ID)
				.fagomraade(ARKIVJOURNALPOST_FAGOMRAADE.name())
				.status(JournalStatusCode.J.name())
				.skjerming(SKJERMING_TYPE_CODE_POL.name())
				.innhold(ARKIVJOURNALPOST_INNHOLD)
				.behandlingstema(ARKIVJOURNALPOST_BEHANDLINGSTEMA)
				.behandlingstemanavn(ARKIVJOURNALPOST_BEHANDLINGSTEMANAVN)
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE_CODE.name(), AVSENDER_MOTTAKER_NAVN, AVSENDER_MOTTAKER_LAND))
				.journalfoerendeEnhet(ARKIVJOURNALPOST_JOURNALFOERENDE_ENHET)
				.journalfoertAvNavn(ARKIVJOURNALPOST_JOURNALFOERT_AV)
				.opprettetAvNavn(ARKIVJOURNALPOST_OPPRETTET_AV_NAVN)
				.relevanteDatoer(ArkivRelevanteDatoer.builder()
						.opprettet(ARKIVJOURNALPOST_DATO_OPPRETTET)
						.build())
				.kanalreferanseId(ARKIVJOURNALPOST_KANALREFERANSE_ID)
				.tilleggsopplysninger(Map.of(TILLEGGSOPPLYSNING_NOKKEL, TILLEGGSOPPLYSNING_VERDI))
				.dokumenter(buildDokumenter());
	}

	private static List<ArkivDokumentinfo> buildDokumenter() {
		return List.of(baseArkivDokumentinfo().build());
	}

	static ArkivDokumentinfo.ArkivDokumentinfoBuilder baseArkivDokumentinfo() {
		return ArkivDokumentinfo.builder()
				.dokumentInfoId(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID)
				.tittel(ARKIVDOKUMENTINFO_TITTEL)
				.brevkode(ARKIVDOKUMENTINFO_BREVKODE)
				.dokumenttypeId(ARKIVDOKUMENTINFO_DOKUMENTTYPE_ID)
				.status(DokumentStatusCode.FERDIGSTILT.name())
				.ferdigDato(ARKIVDOKUMENT_INFO_DATO_FERDIGSTILT)
				.originalJournalpostId(ARKIVJOURNALPOST_JOURNALPOST_ID)
				.skjerming(SKJERMING_TYPE_CODE_POL.name())
				.logiskVedlegg(logiskVedlegg())
				.fildetaljer(List.of(ArkivFildetaljer.builder()
								.skjerming(SKJERMING_TYPE_CODE_POL.name())
								.format(VARIANT_FORMAT_CODE_ARKIV.name())
								.navn(ARKIVFILDETALJER_FILNAVN_1)
								.uuid(ARKIVFILDETALJER_FILUUID_1)
								.type(FILTYPE_1)
								.stoerrelse("1024")
								.build(),
						ArkivFildetaljer.builder()
								.skjerming(null)
								.format(VARIANT_FORMAT_CODE_SLADDET.name())
								.navn(ARKIVFILDETALJER_FILNAVN_2)
								.uuid(ARKIVFILDETALJER_FILUUID_2)
								.type(FILTYPE_2)
								.stoerrelse("2056")
								.build()));
	}

	private static Set<ArkivLogiskVedlegg> logiskVedlegg() {
		return Set.of(new ArkivLogiskVedlegg(LOGISK_VEDLEGG_ID, LOGISK_VEDLEGG_TITTEL));
	}
}
