package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.LogiskVedlegg;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.domain.visningsmodell.Tilleggsopplysning;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static java.util.Collections.emptySet;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.FILTYPE_PDF;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.SKJULT_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.mapJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVDOKUMENTINFO_BREVKODE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVDOKUMENTINFO_DOKUMENTTYPE_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVDOKUMENTINFO_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVDOKUMENT_INFO_DATO_FERDIGSTILT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVFILDETALJER_FILNAVN_1;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVFILDETALJER_FILNAVN_2;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVFILDETALJER_FILUUID_1;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVFILDETALJER_FILUUID_2;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_ANTALL_RETUR;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_AVS_RETUR_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_BEHANDLINGSTEMA;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_BEHANDLINGSTEMANAVN;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_OPPRETTET;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DOKUMENT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_EKSPEDERT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_FAGOMRAADE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_INNHOLD;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_JOURNALFOERENDE_ENHET;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_JOURNALFOERT_AV;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_JOURNALPOST_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_JOURNAL_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_KANALREFERANSE_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_LEST_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_MOTTAT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_OPPRETTET_AV_NAVN;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_SENDT_PRINT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAKRELASJON_FAGSYSTEM;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAKSRELASJON_SAK_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAK_AKTOERID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAK_APPLIKASJON;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAK_FAGSAKNR;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.ARKIVSAK_OPPRETTET_TIDSPUNKT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_LAND;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.BRUKER_ID_ORGANISASJON;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.BRUKER_ID_PERSON;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.FNR;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.LOGISK_VEDLEGG_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.LOGISK_VEDLEGG_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.SKJERMING_TYPE_CODE_POL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.TILLEGGSOPPLYSNING_NOKKEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.TILLEGGSOPPLYSNING_VERDI;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.VARIANT_FORMAT_CODE_ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.VARIANT_FORMAT_CODE_SLADDET;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.baseArkivDokumentinfo;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.baseArkivJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.inngaaendeArkivJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.inngaaendeArkivJournalpostBuilder;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.notatArkivJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.pensjonSakArkivJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.utgaaendeArkivJournalpost;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.utgaaendeArkivJournalpostBuilder;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.DIGITALPOSTKASSE_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.arkivUtsendingsInfoWithDigitalPostadresseOldVarselStructure;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.utsendingsInfoDtoWithFysiskPostadresse;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode.ORGANISASJON;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode.E;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.SDP;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Datotype.DATO_AVS_RETUR;
import static no.nav.saf.domain.kode.Datotype.DATO_DOKUMENT;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Datotype.DATO_JOURNALFOERT;
import static no.nav.saf.domain.kode.Datotype.DATO_LEST;
import static no.nav.saf.domain.kode.Datotype.DATO_REGISTRERT;
import static no.nav.saf.domain.kode.Datotype.DATO_SENDT_PRINT;
import static no.nav.saf.domain.kode.Journalstatus.FEILREGISTRERT;
import static no.nav.saf.domain.kode.Journalstatus.JOURNALFOERT;
import static no.nav.saf.domain.kode.Journalstatus.UTGAAR;
import static no.nav.saf.domain.kode.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.saf.domain.kode.Kanal.LOKAL_UTSKRIFT;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
import static no.nav.saf.domain.kode.Skjerming.POL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class ArkivJournalpostMapperTest {

	@Test
	void shouldMapWithUtgaaendeArkivJournalpost() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, utsendingsInfoDtoWithFysiskPostadresse(), S);
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertCommonMetadata(journalpost);

		assertThat(journalpost.getJournalposttype()).isEqualTo(Journalposttype.U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(Journalstatus.EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(SENTRAL_UTSKRIFT);
		assertThat(journalpost.getKanalnavn()).isEqualTo(SENTRAL_UTSKRIFT.getKanalnavn());
		assertThat(journalpost.getAntallRetur()).isEqualTo(ARKIVJOURNALPOST_ANTALL_RETUR.toString());
		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(6)
				.contains(new RelevantDato(ARKIVJOURNALPOST_JOURNAL_DATO, DATO_JOURNALFOERT),
						new RelevantDato(ARKIVJOURNALPOST_DOKUMENT_DATO, DATO_DOKUMENT),
						new RelevantDato(ARKIVJOURNALPOST_AVS_RETUR_DATO, DATO_AVS_RETUR),
						new RelevantDato(ARKIVJOURNALPOST_SENDT_PRINT_DATO, DATO_SENDT_PRINT),
						new RelevantDato(ARKIVJOURNALPOST_EKSPEDERT_DATO, DATO_EKSPEDERT),
						new RelevantDato(ARKIVJOURNALPOST_LEST_DATO, DATO_LEST));
		assertThat(journalpost.getInnsynsregel()).isEqualTo(ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD);
		assertThat(journalpost.getUtsendingsinfo().getFysiskpostSendt().getAdressetekstKonvolutt()).isEqualTo(FORVENTET_ADRESSETEKST_KONVOLUTT);
	}

	@Test
	void shouldMapJournalpostUtsendingInfoWhenUtsendingsKanalErNAV_NO() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO);
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertCommonMetadata(journalpost);

		Utsendingsinfo utsendingsinfo = journalpost.getUtsendingsinfo();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualTo(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
	}

	@Test
	void shouldMapJournalpostUtsendingInfoWhenUtsendingsKanalErSDP() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, arkivUtsendingsInfoWithDigitalPostadresseOldVarselStructure(), SDP);
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertCommonMetadata(journalpost);

		Utsendingsinfo utsendingsinfo = journalpost.getUtsendingsinfo();
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt().getAdresse()).isEqualTo(DIGITALPOSTKASSE_ADRESSE);
	}

	@Test
	void shouldReturnNullUtsendingInfoWhenUtsendingsKanalErUkjent() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, arkivUtsendingsInfoWithDigitalPostadresseOldVarselStructure(), null);
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertCommonMetadata(journalpost);

		assertThat(journalpost.getUtsendingsinfo()).isNull();
	}

	@Test
	void shouldReturnNullUtsendingInfoWhenJournalpostTypeErInngaaende() {
		ArkivJournalpost arkivJournalpost = inngaaendeArkivJournalpost();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), defaultRequestCache());

		assertCommonMetadata(journalpost);

		assertThat(journalpost.getUtsendingsinfo()).isNull();
	}

	@Test
	void shouldMapWithInngaaendeArkivJournalpost() {
		ArkivJournalpost arkivJournalpost = inngaaendeArkivJournalpost();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), defaultRequestCache());

		assertCommonMetadata(journalpost);

		assertThat(journalpost.getJournalposttype()).isEqualTo(Journalposttype.I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(JOURNALFOERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.NAV_NO);
		assertThat(journalpost.getKanalnavn()).isEqualTo(Kanal.NAV_NO.getKanalnavn());
		assertThat(journalpost.getAntallRetur()).isNull();

		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(2)
				.contains(new RelevantDato(ARKIVJOURNALPOST_MOTTAT_DATO, DATO_REGISTRERT),
						new RelevantDato(ARKIVJOURNALPOST_JOURNAL_DATO, DATO_JOURNALFOERT));
	}

	@Test
	void shouldMapArkivJournalpostWithNotatJournalpost() {
		ArkivJournalpost arkivJournalpost = notatArkivJournalpost();
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertCommonMetadata(journalpost);


		assertThat(journalpost.getJournalposttype()).isEqualTo(Journalposttype.N);
		assertThat(journalpost.getJournalstatus()).isEqualTo(JOURNALFOERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.INGEN_DISTRIBUSJON);

		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(1)
				.contains(new RelevantDato(ARKIVJOURNALPOST_JOURNAL_DATO, DATO_JOURNALFOERT));
	}

	@Test
	void shouldNotMapDokumentinfoWhenIngenTilgangPep5() {
		ArkivJournalpost arkivJournalpost = inngaaendeArkivJournalpost();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.deny(new UkjentEllerTekniskReason()));

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getJournalpostId()).isEqualTo(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID));
		assertThat(journalpost.getDokumenter()).isEmpty();
	}

	@Test
	void shouldMapJournalpostKanalIngenDistribusjonWhenNotat() {
		ArkivJournalpost arkivJournalpost = notatArkivJournalpost();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getKanal()).isEqualTo(INGEN_DISTRIBUSJON);
	}

	@Test
	void shouldMapJournalpostSentralUtskriftWhenNullUtsendingskanalAndFerdigstilt() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(JournalStatusCode.FS, null, null);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getKanal()).isEqualTo(SENTRAL_UTSKRIFT);
	}

	@Test
	void shouldMapJournalpostLokalUtskriftWhenNullUtsendingskanalAndFerdigstiltLokalt() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(JournalStatusCode.FL, null, null);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getKanal()).isEqualTo(LOKAL_UTSKRIFT);
	}


	@Test
	void shouldMapJournalpostPensjonFagsystem() {
		ArkivJournalpost arkivJournalpost = pensjonSakArkivJournalpost();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getSak().getArkivsaksnummer()).isEqualTo(ARKIVSAKSRELASJON_SAK_ID.toString());
		assertThat(journalpost.getSak().getArkivsaksystem()).isEqualTo(PSAK);
		assertThat(journalpost.getSak().getTema()).isEqualTo(Tema.PEN);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(ARKIVSAKSRELASJON_SAK_ID.toString());
	}

	@Test
	void shouldMapJournalpostSkjermingType() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.U.name())
				.skjerming(SkjermingTypeCode.FEIL.name())
				.build();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getSkjerming()).isEqualTo(Skjerming.FEIL);
	}

	@Test
	void shouldMapJournalpostOrgNr() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, null, null);
		RequestCache requestCache = createTilgangBrukerCacheWithOrganisasjonSak();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getBruker().getId()).isEqualTo(BRUKER_ID_ORGANISASJON);
		assertThat(journalpost.getBruker().getType()).isEqualTo(BrukerIdType.ORGNR);
	}

	@Test
	void shouldMapJournalpostWithKassertDokument() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.U.name())
				.dokumenter(List.of(baseArkivDokumentinfo()
						.kassert(true)
						.build()))
				.build();
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter().get(0).getDokumentstatus()).isEqualTo(Dokumentstatus.KASSERT);
	}

	@Test
	void shouldMapJournalpostWithNullDokumentStatus() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.U.name())
				.dokumenter(List.of(baseArkivDokumentinfo()
						.kassert(null)
						.status(null)
						.build()))
				.build();
		RequestCache requestCache = defaultRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter().get(0).getDokumentstatus()).isNull();
	}

	@Test
	void shouldMapSaksbehandlerHarTilgang() {
		ArkivJournalpost arkivJournalpost = pensjonSakArkivJournalpost();
		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(Tema.PEN);
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep7dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(FagsystemCode.toSafArkivsaksystem(arkivJournalpost.saksrelasjon().fagsystem()), arkivJournalpost.saksrelasjon().sakId().toString());

		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();
		requestCache.putDecision(tilgangKeyPep2dLocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantArkiv, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantSladdet, AbacAnswer.deny(new UkjentEllerTekniskReason()));
		requestCache.putDecision(tilgangKeyPep7dLocalCaching, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), true));
	}

	@Test
	void shouldMapSkjultTittelWhenPep2dDeny() {
		ArkivJournalpost arkivJournalpost = pensjonSakArkivJournalpost();

		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();
		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(Tema.PEN);
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		requestCache.putDecision(tilgangKeyPep2dLocalCaching, AbacAnswer.deny(new TemaReason("cause_0013_ikketilgangtiltema", "saf_pep2d", "mangler_tema", Tema.PEN)));
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getTittel()).isEqualTo(SKJULT_TITTEL);
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getTittel()).isEqualTo(SKJULT_TITTEL);
		LogiskVedlegg logiskVedlegg = dokumentInfo.getLogiskeVedlegg().get(0);
		assertThat(logiskVedlegg.getTittel()).isEqualTo(SKJULT_TITTEL);
	}

	@Test
	void shouldNotMapSkjultTittelWhenPep2dDenyAndIsSystem() {
		ArkivJournalpost arkivJournalpost = pensjonSakArkivJournalpost();

		RequestCache requestCache = new RequestCache(true);
		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(Tema.PEN);
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		requestCache.putDecision(tilgangKeyPep2dLocalCaching, AbacAnswer.deny(new TemaReason("cause_0013_ikketilgangtiltema", "saf_pep2d", "mangler_tema", Tema.PEN)));
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getTittel()).isEqualTo(ARKIVJOURNALPOST_INNHOLD);
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getTittel()).isEqualTo(ARKIVDOKUMENTINFO_TITTEL);
		LogiskVedlegg logiskVedlegg = dokumentInfo.getLogiskeVedlegg().get(0);
		assertThat(logiskVedlegg.getTittel()).isEqualTo(LOGISK_VEDLEGG_TITTEL);
	}

	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenJournalstatusMottatt() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.status(JournalStatusCode.M.name())
				.build();

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache(false);
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantArkiv, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantSladdet, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getTittel()).isEqualTo(ARKIVJOURNALPOST_INNHOLD);
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getTittel()).isEqualTo(ARKIVDOKUMENTINFO_TITTEL);
		LogiskVedlegg logiskVedlegg = dokumentInfo.getLogiskeVedlegg().get(0);
		assertThat(logiskVedlegg.getTittel()).isEqualTo(LOGISK_VEDLEGG_TITTEL);
		assertThat(journalpost.getDokumenter())
				.hasSize(1)
				.flatExtracting(DokumentInfo::getDokumentvarianter)
				.extracting(Dokumentvariant::isSaksbehandlerHarTilgang)
				.containsExactly(true, true);
	}

	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenJournalstatusUtgaarAndIsMidlertidigIngenSak() {
		ArkivJournalpost arkivJournalpost = inngaaendeArkivJournalpostBuilder()
				.saksrelasjon(null)
				.build();

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache(false);
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantArkiv, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantSladdet, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter())
				.hasSize(1)
				.flatExtracting(DokumentInfo::getDokumentvarianter)
				.extracting(Dokumentvariant::isSaksbehandlerHarTilgang)
				.containsExactly(true, true);
	}

	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenTemaUkjent() {
		ArkivJournalpost arkivJournalpost = inngaaendeArkivJournalpostBuilder()
				.fagomraade(FagomradeCode.UKJ.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), false,
						new ArkivSak(null, ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.build();

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID), VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache(false);
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantArkiv, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep6dLocalCachingVariantSladdet, AbacAnswer.permit());

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter())
				.hasSize(1)
				.flatExtracting(DokumentInfo::getDokumentvarianter)
				.extracting(Dokumentvariant::isSaksbehandlerHarTilgang)
				.containsExactly(true, true);
	}

	@Test
	void shouldUseArkivsakTemaWhenSakstilknyttetJournalpost() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), false,
						new ArkivSak(FagomradeCode.STO.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.fagomraade(FagomradeCode.AAP.name())
				.build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), arkivsakCacheRequestCache);

		assertThat(journalpost.getTema()).isEqualTo(Tema.STO);
		assertThat(journalpost.getTemanavn()).isEqualTo(Tema.STO.getTemanavn());
	}

	@Test
	void shouldUseJournalpostTemaWhenIkkeSakstilknyttetJournalpostIngenSaksrelasjon() {
		ArkivJournalpost journalpostDto = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(null)
				.fagomraade(FagomradeCode.AAP.name())
				.build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapJournalpost(journalpostDto, emptySet(), arkivsakCacheRequestCache);

		assertThat(journalpost.getTema()).isEqualTo(Tema.AAP);
		assertThat(journalpost.getTemanavn()).isEqualTo(Tema.AAP.getTemanavn());
	}

	@Test
	void shouldUseJournalpostTemaWhenIkkeSakstilknyttetJournalpostIngenSakId() {
		ArkivJournalpost journalpostDto = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(new ArkivSaksrelasjon(null, ARKIVSAKRELASJON_FAGSYSTEM.name(), false, null))
				.fagomraade(FagomradeCode.AAP.name())
				.build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapJournalpost(journalpostDto, emptySet(), arkivsakCacheRequestCache);

		assertThat(journalpost.getTema()).isEqualTo(Tema.AAP);
		assertThat(journalpost.getTemanavn()).isEqualTo(Tema.AAP.getTemanavn());
	}

	@ParameterizedTest
	@EnumSource(names = {"I", "N"})
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsInngaaendeAndNotat(JournalpostTypeCode type) {
		RequestCache requestCache = defaultRequestCache();
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(type.name())
				.build();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getDokumenter().get(0).getBrevkode()).isEqualTo(ARKIVDOKUMENTINFO_BREVKODE);
	}

	@Test
	void shouldMapDokumenttypeIdAsBrevkodeWhenJournalpostIsUtgaaende() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost(E, null, null);

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), defaultRequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode()).isEqualTo(ARKIVDOKUMENTINFO_DOKUMENTTYPE_ID);
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsUtgaaendeAndDokumenttypeIdNotSet() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpostBuilder(E, null, null)
				.dokumenter(List.of(baseArkivDokumentinfo()
						.dokumenttypeId(null)
						.build()))
				.build();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), defaultRequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode()).isEqualTo(ARKIVDOKUMENTINFO_BREVKODE);
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForPerson() {
		ArkivJournalpost journalpostDto = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(null)
				.bruker(new ArkivBruker(BRUKER_ID_PERSON, BrukerTypeCode.PERSON))
				.build();

		Journalpost journalpost = mapJournalpost(journalpostDto, emptySet(), new RequestCache(false));

		assertThat(journalpost.getBruker().getId()).isEqualTo(BRUKER_ID_PERSON);
		assertThat(journalpost.getBruker().getType()).isEqualTo(BrukerIdType.FNR);
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjon() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(null)
				.bruker(new ArkivBruker(BRUKER_ID_ORGANISASJON, ORGANISASJON))
				.build();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), new RequestCache(false));

		assertThat(journalpost.getBruker().getId()).isEqualTo(BRUKER_ID_ORGANISASJON);
		assertThat(journalpost.getBruker().getType()).isEqualTo(BrukerIdType.ORGNR);
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjonAndTrailingBlanks() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.saksrelasjon(null)
				.bruker(new ArkivBruker(BRUKER_ID_ORGANISASJON + "   ", ORGANISASJON))
				.build();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), new RequestCache(false));

		assertThat(journalpost.getBruker().getId()).isEqualTo(BRUKER_ID_ORGANISASJON);
		assertThat(journalpost.getBruker().getType()).isEqualTo(BrukerIdType.ORGNR);
	}

	@Test
	void shouldMapJournalstatusFeilregistrertWhenJournalfoertAndIsFeilregistrert() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpostBuilder(JournalStatusCode.J, null, null)
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), true,
						new ArkivSak(ARKIVJOURNALPOST_FAGOMRAADE.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.build();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), requestCache);

		assertThat(journalpost.getJournalstatus()).isEqualTo(FEILREGISTRERT);
	}

	@Test
	void shouldMapJournalstatusUtgaarWhenUtgaarAndIsFeilregistrert() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpostBuilder(JournalStatusCode.J, null, null)
				.saksrelasjon(new ArkivSaksrelasjon(ARKIVSAKSRELASJON_SAK_ID, ARKIVSAKRELASJON_FAGSYSTEM.name(), true,
						new ArkivSak(ARKIVJOURNALPOST_FAGOMRAADE.name(), ARKIVSAK_AKTOERID, null, ARKIVSAK_FAGSAKNR, ARKIVSAK_APPLIKASJON, ARKIVSAK_OPPRETTET_TIDSPUNKT)))
				.status(JournalStatusCode.U.name())
				.build();

		Journalpost journalpost = mapJournalpost(arkivJournalpost, emptySet(), createTilgangBrukerRequestCache());

		assertThat(journalpost.getJournalstatus()).isEqualTo(UTGAAR);
	}

	private RequestCache defaultRequestCache() {
		RequestCache requestCache = createArkivsakCacheRequestCache();
		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(FagomradeCode.toSafTema(ARKIVJOURNALPOST_FAGOMRAADE));
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID), String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		requestCache.putDecision(tilgangKeyPep2dLocalCaching, AbacAnswer.permit());
		requestCache.putDecision(tilgangKeyPep5LocalCaching, AbacAnswer.permit());
		return requestCache;
	}

	private void assertCommonMetadata(Journalpost journalpost) {
		assertThat(journalpost.getJournalpostId()).isEqualTo(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID));
		assertThat(journalpost.getTittel()).isEqualTo(ARKIVJOURNALPOST_INNHOLD);
		assertThat(journalpost.getTema()).isEqualTo(FagomradeCode.toSafTema(ARKIVJOURNALPOST_FAGOMRAADE));
		assertThat(journalpost.getJournalfortAvNavn()).isEqualTo(ARKIVJOURNALPOST_JOURNALFOERT_AV);
		assertThat(journalpost.getBehandlingstema()).isEqualTo(ARKIVJOURNALPOST_BEHANDLINGSTEMA);
		assertThat(journalpost.getBehandlingstemanavn()).isEqualTo(ARKIVJOURNALPOST_BEHANDLINGSTEMANAVN);
		assertThat(journalpost.getAvsenderMottaker().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsenderMottaker().getType()).isEqualTo(AvsenderMottakerIdType.FNR);
		assertThat(journalpost.getAvsenderMottaker().getNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getAvsenderMottaker().getLand()).isEqualTo(AVSENDER_MOTTAKER_LAND);
		assertThat(journalpost.getAvsenderMottaker().isErLikBruker()).isFalse();
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsenderMottakerNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getAvsenderMottakerLand()).isEqualTo(AVSENDER_MOTTAKER_LAND);
		assertThat(journalpost.getJournalfoerendeEnhet()).isEqualTo(ARKIVJOURNALPOST_JOURNALFOERENDE_ENHET);
		assertThat(journalpost.getOpprettetAvNavn()).isEqualTo(ARKIVJOURNALPOST_OPPRETTET_AV_NAVN);
		assertThat(journalpost.getSkjerming()).isEqualTo(POL);
		assertThat(journalpost.getDatoOpprettet()).isEqualTo(ARKIVJOURNALPOST_DATO_OPPRETTET.atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime());
		assertThat(journalpost.getBruker().getId()).isEqualTo(ARKIVSAK_AKTOERID);
		assertThat(journalpost.getEksternReferanseId()).isEqualTo(ARKIVJOURNALPOST_KANALREFERANSE_ID);
		assertThat(journalpost.getTilleggsopplysninger())
				.hasSize(1)
				.extracting(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi)
				.containsExactly(tuple(TILLEGGSOPPLYSNING_NOKKEL, TILLEGGSOPPLYSNING_VERDI));
		assertCommonDokumenter(journalpost.getDokumenter());
	}

	private void assertCommonDokumenter(List<DokumentInfo> dokumenter) {
		assertThat(dokumenter).hasSize(1);
		DokumentInfo dokumentInfo1 = dokumenter.get(0);
		assertThat(dokumentInfo1.getDokumentInfoId()).isEqualTo(String.valueOf(ARKIVDOKUMENTINFO_DOKUMENT_INFO_ID));
		assertThat(dokumentInfo1.getDatoFerdigstilt()).isEqualTo(ARKIVDOKUMENT_INFO_DATO_FERDIGSTILT.atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime());
		assertThat(dokumentInfo1.getOriginalJournalpostId()).isEqualTo(String.valueOf(ARKIVJOURNALPOST_JOURNALPOST_ID));
		assertThat(dokumentInfo1.getSkjerming()).isEqualTo(POL);
		assertThat(dokumentInfo1.getDokumentstatus()).isEqualTo(Dokumentstatus.FERDIGSTILT);
		assertThat(dokumentInfo1.getLogiskeVedlegg())
				.extracting(LogiskVedlegg::getLogiskVedleggId, LogiskVedlegg::getTittel)
				.containsExactly(tuple(LOGISK_VEDLEGG_ID.toString(), LOGISK_VEDLEGG_TITTEL));

		assertCommonDokumentvarianter(dokumentInfo1.getDokumentvarianter());
	}

	private void assertCommonDokumentvarianter(List<Dokumentvariant> dokumentvarianter) {
		assertThat(dokumentvarianter)
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFilnavn,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::isSaksbehandlerHarTilgang,
						Dokumentvariant::getSkjerming,
						Dokumentvariant::getFilstoerrelse,
						Dokumentvariant::getFiltype)
				.hasSize(2)
				.containsExactlyInAnyOrder(
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(),
								ARKIVFILDETALJER_FILNAVN_1,
								ARKIVFILDETALJER_FILUUID_1,
								false,
								POL,
								1024,
								FILTYPE_PDF),
						tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(),
								ARKIVFILDETALJER_FILNAVN_2,
								ARKIVFILDETALJER_FILUUID_2,
								false,
								null,
								2056,
								FILTYPE_PDF));
	}

	private RequestCache createArkivsakCacheRequestCache() {
		RequestCache requestCache = new RequestCache(false);
		requestCache.putArkivsak(
				Arkivsak.builder()
						.arkivsaksnummer(String.valueOf(ARKIVSAKSRELASJON_SAK_ID))
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.aktoerId(ARKIVSAK_AKTOERID)
						.tema(Tema.STO)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCache() {
		RequestCache requestCache = new RequestCache(false);
		requestCache.putTilgangBruker(
				TilgangBruker.builder()
						.foedselsnr(FNR)
						.aktoerId(ARKIVSAK_AKTOERID)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCachePSAK() {
		RequestCache requestCache = new RequestCache(false);
		Arkivsak pensjonArkivsak = Arkivsak.builder()
				.aktoerId(ARKIVSAK_AKTOERID)
				.arkivsaksystem(PSAK)
				.arkivsaksnummer(ARKIVSAKSRELASJON_SAK_ID.toString())
				.fagsakId(ARKIVSAKSRELASJON_SAK_ID.toString())
				.fagsaksystem("PP01")
				.tema(Tema.PEN)
				.build();
		requestCache.putArkivsak(
				pensjonArkivsak
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerCacheWithOrganisasjonSak() {
		RequestCache requestCache = new RequestCache(false);
		requestCache.putTilgangBruker(
				TilgangBruker.builder()
						.orgnummer(BRUKER_ID_ORGANISASJON)
						.build()
		);
		return requestCache;
	}
}