package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.tilgangskontroll.RequestCache;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper.FILTYPE_PDF;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AKTOER_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ANTALL_RETUR;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ARKIVSAK_NR;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_LAND;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVS_RETUR_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.BEHANDLINGSTEMA;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.BEHANDLINGSTEMANAVN;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.BREVKODE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.BRUKER_ID_ORGANISASJON;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.BRUKER_ID_PERSON;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DATO_FERDIGSTILT;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DATO_OPPRETTET;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DIGITALKONTAKT_INFORMASJON;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DIGITALPOSTKASSEADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DOKUMENT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DOKUMENT_INFO_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.EKSPEDERT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.FAGOMRADE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.FAKSYSTEM_CODE;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.INNHOLD;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.JOURNALFOERENDE_ENHET;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.JOURNALFOERT_AV;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.JOURNALPOST_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.JOURNAL_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.KANAL_REFERANSE_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.LEST_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.LOGISK_VEDLEGG_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.LOGISK_VEDLEGG_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.MOTTAT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.OPPRETTET_AV_NAVN;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ORG_NR;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.PHONENUMMER;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.SAKS_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.SENDT_PRINT_DATO;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.SMS_VARSELTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.TILLEGGSOPPLYSNING_NOKKEL;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.TILLEGGSOPPLYSNING_VERDI;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_KONTAKTINFO_3;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING3;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TEKST1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TEKST2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TITTEL1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TITTEL2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.createDigitalPostadresse;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.createDitttNavVarsel;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.createFysiskPostadresseDto;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode.E;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode.M;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode.U;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.SDP;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Datotype.DATO_AVS_RETUR;
import static no.nav.saf.domain.kode.Datotype.DATO_DOKUMENT;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Datotype.DATO_JOURNALFOERT;
import static no.nav.saf.domain.kode.Datotype.DATO_LEST;
import static no.nav.saf.domain.kode.Datotype.DATO_REGISTRERT;
import static no.nav.saf.domain.kode.Datotype.DATO_SENDT_PRINT;
import static no.nav.saf.domain.kode.Dokumentstatus.KASSERT;
import static no.nav.saf.domain.kode.Journalstatus.FEILREGISTRERT;
import static no.nav.saf.domain.kode.Journalstatus.JOURNALFOERT;
import static no.nav.saf.domain.kode.Journalstatus.UTGAAR;
import static no.nav.saf.domain.kode.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.saf.domain.kode.Kanal.LOKAL_UTSKRIFT;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
import static no.nav.saf.domain.kode.Kanal.UKJENT;
import static no.nav.saf.domain.kode.Skjerming.POL;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.ORGNR;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(MockitoExtension.class)
class JournalpostDtoMapperTest {

	private final JournalpostDtoMapper mapper = new JournalpostDtoMapper();

	@Test
	void shouldMapJournalpostDtoWithUtgaaendeJournalpost() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createFysiskPostadresseDto(), S);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(U.toString(), journalpost.getJournalposttype().toString());
		assertEquals(E.toSafJournalstatus(), journalpost.getJournalstatus());
		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
		assertEquals(ANTALL_RETUR, journalpost.getAntallRetur());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, DATO_JOURNALFOERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(DOKUMENT_DATO, DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(AVS_RETUR_DATO, DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(SENDT_PRINT_DATO, DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(EKSPEDERT_DATO, DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(LEST_DATO, DATO_LEST)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(MOTTAT_DATO, DATO_REGISTRERT))));
		assertEquals(ADRESSETEKST_KONVOLUTT, journalpost.getUtsendingsinfo().getFysiskpostSendt().getAdressetekstKonvolutt());
	}

	@Test
	void shouldMapJournalpostUtsendingInfoWhenUtsendingsKanalErNAV_NO() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(VARSEL_MELDING1, DIGITALKONTAKT_INFORMASJON), NAV_NO);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertNull(journalpost.getUtsendingsinfo().getFysiskpostSendt());
		assertNull(journalpost.getUtsendingsinfo().getDigitalpostSendt());
		assertEquals(VARSEL_TITTEL1, journalpost.getUtsendingsinfo().getEpostVarselSendt().getTittel());
		assertEquals(VARSEL_TEKST1, journalpost.getUtsendingsinfo().getEpostVarselSendt().getVarslingstekst());
		assertEquals("epostaddress3@nav.no", journalpost.getUtsendingsinfo().getEpostVarselSendt().getAdresse());
	}

	@Test
	void shouldMapJournalpostUtsendingInfoWhenUtsendingsKanalErSDP() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDigitalPostadresse(), SDP);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertNull(journalpost.getUtsendingsinfo().getEpostVarselSendt());
		assertNull(journalpost.getUtsendingsinfo().getSmsVarselSendt());
		assertEquals(DIGITALPOSTKASSEADRESSE, journalpost.getUtsendingsinfo().getDigitalpostSendt().getAdresse());
	}

	@Test
	void shouldReturnNullUtsendingInfoWhenUtsendingsKanalErUkjent() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDigitalPostadresse(), null);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertNull(journalpost.getUtsendingsinfo());
	}

	@Test
	void shouldReturnNullUtsendingInfoWhenJournalpostTypeErUtgående() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertCommonMetadata(journalpost);

		assertNull(journalpost.getUtsendingsinfo());
	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.I, journalpost.getJournalposttype());
		assertEquals(JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), UKJENT.getKanalnavn());
		assertNull(journalpost.getAntallRetur());

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(DOKUMENT_DATO, DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(AVS_RETUR_DATO, DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(SENDT_PRINT_DATO, DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(EKSPEDERT_DATO, DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(LEST_DATO, DATO_LEST))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(MOTTAT_DATO, DATO_REGISTRERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, DATO_JOURNALFOERT)));
	}

	@Test
	void shouldMapJournalpostDtoWithNotatJournalpost() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, true);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.N, journalpost.getJournalposttype());
		assertEquals(JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), INGEN_DISTRIBUSJON.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, DATO_JOURNALFOERT)));
	}

	@Test
	void shouldNotMapDokumentinfoIngenTilgangPep5() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, false);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost, is(notNullValue()));
		assertThat(journalpost.getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
		assertTrue(journalpost.getDokumenter().isEmpty());
	}

	@Test
	void shouldMapJournalpostIngenDistribusjon() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(INGEN_DISTRIBUSJON.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgSentral() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FS, null, null);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgLokal() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FL, null, null);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(LOKAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}


	@Test
	void shouldMapJournalpostPensjonFagsystem() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoPenSaksrelasjonDto();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(ARKIVSAK_NR, journalpost.getSak().getArkivsaksnummer());
		assertEquals(PSAK.name(), journalpost.getSak().getArkivsaksystem().name());
	}

	@Test
	void shouldMapJournalpostSkjermingType() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoPenSaksrelasjonDto();
		journalpostDto.setSkjerming(SkjermingTypeCode.FEIL);
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Skjerming.FEIL, journalpost.getSkjerming());
	}

	@Test
	void shouldMapJournalpostOrgNr() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, null, null);
		RequestCache requestCache = createTilgangBrukerCacheWithOrganisasjonSak();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(ORG_NR, journalpost.getBruker().getId());
	}

	@Test
	void shouldMapJournalpostWithKasserDokument() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, null, null);
		journalpostDto.getDokumenter().get(0).setKassert(true);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(KASSERT, journalpost.getDokumenter().get(0).getDokumentstatus());
	}

	@Test
	void shouldMapJournalpostWithNullDokumentStatus() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, null, null);
		journalpostDto.getDokumenter().get(0).setDokumentstatus(null);
		journalpostDto.getDokumenter().get(0).setKassert(null);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertNull(journalpost.getDokumenter().get(0).getDokumentstatus());
	}


	@Test
	void shouldMapSaksbehandlerHarTilgang() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoPenSaksrelasjonDto();

		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(journalpostDto.getFagomrade().name());

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);

		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());

		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());


		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep2dLocalCaching, TRUE);
		requestCache.putObject(tilgangKeyPep5LocalCaching, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, FALSE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		Assertions.assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), true));
	}

	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenJournalstatusMottatt() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setJournalstatus(M);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep5LocalCaching, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, TRUE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter(), hasSize(1));
		journalpost.getDokumenter().forEach(dokumentInfo ->
				dokumentInfo.getDokumentvarianter().forEach(dokumentvariant -> {
					assertTrue(dokumentvariant.isSaksbehandlerHarTilgang());
				}));
	}

	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenJournalstatusUtgaarAndIsMidlertidigIngenSak() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setJournalstatus(JournalStatusCode.U);
		journalpostDto.setSaksrelasjon(null);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep5LocalCaching, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, TRUE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter(), hasSize(1));
		journalpost.getDokumenter().forEach(dokumentInfo ->
				dokumentInfo.getDokumentvarianter().forEach(dokumentvariant -> {
					assertTrue(dokumentvariant.isSaksbehandlerHarTilgang());
				}));
	}


	@Test
	void shouldHaveSaksbehandlerHarTilgangTrueWhenTemaUkjent() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setFagomrade(FagomradeCode.UKJ);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep5LocalCaching, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, TRUE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter(), hasSize(1));
		journalpost.getDokumenter().forEach(dokumentInfo ->
				dokumentInfo.getDokumentvarianter().forEach(dokumentvariant -> {
					assertTrue(dokumentvariant.isSaksbehandlerHarTilgang());
				}));
	}

	@Test
	void shouldUseArkivsakTemaWhenSakstilknyttetJournalpost() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE, null,
						null, null, null, null, null, null))
				.fagomrade(FagomradeCode.AAP).build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, arkivsakCacheRequestCache);

		assertThat(journalpost.getTema(), is(Tema.STO));
		assertThat(journalpost.getTemanavn(), is(Tema.STO.getTemanavn()));
	}

	@Test
	void shouldUseJournalpostTemaWhenIkkeSakstilknyttetJournalpostIngenSaksrelasjon() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.fagomrade(FagomradeCode.AAP).build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, arkivsakCacheRequestCache);

		assertThat(journalpost.getTema(), is(Tema.AAP));
		assertThat(journalpost.getTemanavn(), is(Tema.AAP.getTemanavn()));
	}

	@Test
	void shouldUseJournalpostTemaWhenIkkeSakstilknyttetJournalpostIngenSakId() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(null, false, FAKSYSTEM_CODE, null, null,
						null, null, null, null, null))
				.fagomrade(FagomradeCode.AAP).build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, arkivsakCacheRequestCache);

		assertThat(journalpost.getTema(), is(Tema.AAP));
		assertThat(journalpost.getTemanavn(), is(Tema.AAP.getTemanavn()));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsInngaaende() {
		RequestCache requestCache = pep5RequestCache();
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsNotat() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	@Test
	void shouldMapDokumenttypeIdAsBrevkodeWhenJournalpostIsUtgaaende() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, null, null);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(JournalpostDtoTestObjects.DOKUMENTTYPE_ID));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsUtgaaendeAndDokumenttypeIdNotSet() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, null, null);
		journalpostDto.getDokumenter().get(0).setDokumenttypeId(null);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	// Se https://jira.adeo.no/browse/MMA-3076
	@Test
	void shouldMapFromFagomradeOKOToTemaSTO() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(VARSEL_MELDING2, DIGITALKONTAKT_INFORMASJON), NAV_NO);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertEquals(VARSEL_TITTEL2, journalpost.getUtsendingsinfo().getEpostVarselSendt().getTittel());
		assertEquals(VARSEL_TEKST2, journalpost.getUtsendingsinfo().getEpostVarselSendt().getVarslingstekst());
		assertThat(journalpost.getTema(), is(Tema.STO));
	}

	@Test
	void shouldMapSmsTekstOgKontaktInfo() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(VARSEL_MELDING3, VARSEL_KONTAKTINFO_3), NAV_NO);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertEquals(SMS_VARSELTEKST, journalpost.getUtsendingsinfo().getSmsVarselSendt().getVarslingstekst());
		assertEquals(PHONENUMMER, journalpost.getUtsendingsinfo().getSmsVarselSendt().getAdresse());
		assertNull(journalpost.getUtsendingsinfo().getEpostVarselSendt());
		assertNull(journalpost.getUtsendingsinfo().getDigitalpostSendt());
		assertThat(journalpost.getTema(), is(Tema.STO));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForPerson() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(BRUKER_ID_PERSON)
						.brukerIdType(PERSON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(BRUKER_ID_PERSON));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.FNR));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjon() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(BRUKER_ID_ORGANISASJON)
						.brukerIdType(ORGANISASJON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(BRUKER_ID_ORGANISASJON));
		assertThat(journalpost.getBruker().getType(), is(ORGNR));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjonAndTrailingBlanks() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(BRUKER_ID_ORGANISASJON + "   ")
						.brukerIdType(ORGANISASJON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(BRUKER_ID_ORGANISASJON));
		assertThat(journalpost.getBruker().getType(), is(ORGNR));
	}

	@Test
	void shouldMapJournalstatusFeilregistrertWhenJournalfoertAndIsFeilregistrert() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.J, null, null);
		journalpostDto.getSaksrelasjon().setFeilregistrert(true);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(FEILREGISTRERT, journalpost.getJournalstatus());
	}

	@Test
	void shouldMapJournalstatusUtgaarWhenUtgaarAndIsFeilregistrert() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.U, createFysiskPostadresseDto(), S);
		journalpostDto.getSaksrelasjon().setFeilregistrert(true);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(UTGAAR, journalpost.getJournalstatus());
	}

	private RequestCache pep5RequestCache() {
		RequestCache requestCache = createArkivsakCacheRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, true);
		return requestCache;
	}

	private void assertCommonMetadata(Journalpost journalpost) {
		assertEquals(Long.toString(JOURNALPOST_ID), journalpost.getJournalpostId());
		assertEquals(INNHOLD, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafTema(FAGOMRADE), journalpost.getTema());
		assertEquals(JOURNALFOERT_AV, journalpost.getJournalfortAvNavn());
		assertEquals(BEHANDLINGSTEMA, journalpost.getBehandlingstema());
		assertEquals(BEHANDLINGSTEMANAVN, journalpost.getBehandlingstemanavn());
		assertThat(journalpost.getAvsenderMottaker().getId(), is(AVSENDER_MOTTAKER_ID));
		assertThat(journalpost.getAvsenderMottaker().getType(), is(AVSENDER_MOTTAKER_ID_TYPE));
		assertThat(journalpost.getAvsenderMottaker().getNavn(), is(AVSENDER_MOTTAKER_NAVN));
		assertThat(journalpost.getAvsenderMottaker().getLand(), is(AVSENDER_MOTTAKER_LAND));
		assertFalse(journalpost.getAvsenderMottaker().isErLikBruker());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_NAVN, journalpost.getAvsenderMottakerNavn());
		assertEquals(AVSENDER_MOTTAKER_LAND, journalpost.getAvsenderMottakerLand());
		assertEquals(JOURNALFOERENDE_ENHET, journalpost.getJournalfoerendeEnhet());
		assertEquals(OPPRETTET_AV_NAVN, journalpost.getOpprettetAvNavn());
		assertEquals(POL, journalpost.getSkjerming());

		assertEquals(LocalDateTime.from(DATO_OPPRETTET.toInstant()
				.atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));
		assertEquals(1, journalpost.getDokumenter().size());
		assertThat(journalpost.getTilleggsopplysninger(), hasSize(1));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel(), is(TILLEGGSOPPLYSNING_NOKKEL));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi(), is(TILLEGGSOPPLYSNING_VERDI));

		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertEquals(DOKUMENT_INFO_ID, dokumentInfo1.getDokumentInfoId());
		assertThat(dokumentInfo1.getDatoFerdigstilt(), equalTo(LocalDateTime.from(DATO_FERDIGSTILT.toInstant()
				.atZone(ZoneId.systemDefault()))));
		assertEquals(Long.toString(JOURNALPOST_ID), dokumentInfo1.getOriginalJournalpostId());
		assertEquals(POL, dokumentInfo1.getSkjerming());
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getLogiskVedleggId(), is(LOGISK_VEDLEGG_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is(LOGISK_VEDLEGG_TITTEL));

		Assertions.assertThat(dokumentInfo1.getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFilnavn,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::isSaksbehandlerHarTilgang,
						Dokumentvariant::getSkjerming,
						Dokumentvariant::getFiltype)
				.hasSize(2)
				.containsExactlyInAnyOrder(
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(),
								JournalpostDtoTestObjects.FILNAVN_1,
								JournalpostDtoTestObjects.FILUUID_1, false,
								POL,
								FILTYPE_PDF),
						tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(),
								JournalpostDtoTestObjects.FILNAVN_2,
								JournalpostDtoTestObjects.FILUUID_2,
								false,
								null,
								FILTYPE_PDF));

		assertEquals(Dokumentstatus.FERDIGSTILT, dokumentInfo1.getDokumentstatus());
		assertEquals(AKTOER_ID, journalpost.getBruker().getId());
		assertEquals(KANAL_REFERANSE_ID, journalpost.getEksternReferanseId());
	}

	private RequestCache createArkivsakCacheRequestCache() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(SAKS_ID + Arkivsakssystem.GSAK.name(),
				Arkivsak.builder()
						.aktoerId(AKTOER_ID)
						.tema(Tema.STO)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCache() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(TILGANG_BRUKER,
				TilgangBruker.builder()
						.foedselsnr(JournalpostDtoTestObjects.FNR)
						.aktoerId(AKTOER_ID)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCachePSAK() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(SAKS_ID + PSAK.name(),
				Arkivsak.builder()
						.aktoerId(AKTOER_ID)
						.arkivsaksystem(PSAK)
						.arkivsaksnummer(ARKIVSAK_NR)
						.tema(Tema.PEN)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerCacheWithOrganisasjonSak() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(TILGANG_BRUKER,
				TilgangBruker.builder()
						.orgnummer(ORG_NR)
						.build()
		);
		return requestCache;
	}
}