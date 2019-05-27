package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.LogiskVedleggDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.TilleggsopplysningDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.VariantDto;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;

import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(MockitoExtension.class)
class JournalpostDtoMapperTest {
	private static final String DOKUMENT_INFO_ID = "1234";
	private static final VariantFormatCode VARIANT_FORMAT_CODE_ARKIV = VariantFormatCode.ARKIV;
	private static final VariantFormatCode VARIANT_FORMAT_CODE_SLADDET = VariantFormatCode.SLADDET;
	private static final SkjermingTypeCode SKJERMING_TYPE_CODE_POL = SkjermingTypeCode.POL;
	private static final String BREVKODE = "brevkodeX";

	private static final long JOURNALPOST_ID = 417457822L;
	private static final String INNHOLD = "MASKERT_FELT";
	private static final Date DATO_OPPRETTET = new Date(1000L);
	private static final Date AVS_RETUR_DATO = new Date(2000L);
	private static final Date SENDT_PRINT_DATO = new Date(3000L);
	private static final Date EKSPEDERT_DATO = new Date(4000L);
	private static final Date DOKUMENT_DATO = new Date(5000L);
	private static final Date JOURNAL_DATO = new Date(6000L);
	private static final Date MOTTAT_DATO = new Date(7000L);
	private static final Date DATO_FERDIGSTILT = new Date(8000L);
	private static final String FNR = "***gammelt_fnr***";
	private static final String AKTOER_ID = "***gammelt_fnr***31";
	private static final String SAKS_ID = "12345";
	private static final String ORG_NR = "54321";
	private static final String ARKIVSAK_NR = "1337";
	private static final FagsystemCode FAKSYSTEM_CODE = FagsystemCode.FS22;

	private static final FagomradeCode FAGOMRADE = FagomradeCode.STO;
	private static final String JOURNALFOERT_AV = "Automatisk jobb";
	private static final String BEHANDLINGSTEMA = "ab0072";
	private static final String BEHANDLINGSTEMANAVN = "Foreldrepenger ved adopsjon";
	private static final String AVSENDER_MOTTAKER_NAVN = "Bjarne Betjent";
	private static final String AVSENDER_MOTTAKER_LAND = "NO";
	private static final String JOURNALFOERENDE_ENHET = "2990";
	private static final String OPPRETTET_AV_NAVN = "Max Mekker";
	private static final String TILLEGGSOPPLYSNING_NOKKEL = "bucid";
	private static final String TILLEGGSOPPLYSNING_VERDI = "21521";
	private static final String FILNAVN_1 = "filnavn1";
	private static final String FILNAVN_2 = "filnavn2";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final AvsenderMottakerIdType AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdType.FNR;
	private static final String LOGISK_VEDLEGG_ID = "logisk1";
	private static final String LOGISK_VEDLEGG_TITTEL = "logisktittel";
	private static final String DOKUMENTTYPE_ID = "00000001";

	private final JournalpostDtoMapper mapper = new JournalpostDtoMapper();

	@Mock
	private OidcValidatorTool oidcValidatorTool;

	@Test
	void shouldMapJournalpostDtoWithUtgaaendeJournalpost() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.U.toString(), journalpost.getJournalposttype().toString());
		assertEquals(JournalStatusCode.E.toSafJournalstatus(), journalpost.getJournalstatus());
		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT))));
	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.I, journalpost.getJournalposttype());
		assertEquals(Journalstatus.JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), Kanal.UKJENT.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
	}

	@Test
	void shouldMapJournalpostDtoWithNotatJournalpost() {
		JournalpostDto journalpostDto = buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, true);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.N, journalpost.getJournalposttype());
		assertEquals(Journalstatus.JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), Kanal.INGEN_DISTRIBUSJON.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
	}

	@Test
	void shouldNotMapDokumentinfoIngenTilgangPep5() {
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();
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
		JournalpostDto journalpostDto = buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.INGEN_DISTRIBUSJON.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgSentral() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FS);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgLokal() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FL);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.LOKAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}


	@Test
	void shouldMapJournalpostPensjonFagsystem() {
		JournalpostDto journalpostDto = buildJournalpostDtoPenSaksrelasjonDto();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(ARKIVSAK_NR, journalpost.getSak().getArkivsaksnummer());
		assertEquals(Arkivsakssystem.PSAK.name(), journalpost.getSak().getArkivsaksystem().name());
	}

	@Test
	void shouldMapJournalpostOrgNr() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = createTilgangBrukerCacheWithOrganisasjonSak();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(ORG_NR, journalpost.getBruker().getId());
	}

	@Test
	void shouldMapJournalpostWithKasserDokument() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.getDokumenter().get(0).setKassert(true);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Dokumentstatus.KASSERT, journalpost.getDokumenter().get(0).getDokumentstatus());
	}

	@Test
	void shouldMapJournalpostWithNullDokumentStatus() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.getDokumenter().get(0).setDokumentstatus(null);
		journalpostDto.getDokumenter().get(0).setKassert(null);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(null, journalpost.getDokumenter().get(0).getDokumentstatus());
	}


	@Test
	void shouldMapSaksbehandlerHarTilgang() {
		JournalpostDto journalpostDto = buildJournalpostDtoPenSaksrelasjonDto();

		String tilgangKeyPep2dLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(journalpostDto.getFagomrade().name());

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);

		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());

		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());


		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep2dLocalCaching, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep5LocalCaching, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, Boolean.FALSE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		Assertions.assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), true));
	}

	@Test
	void shouldHaveSaksbehandlerHaveTilgangTrueWhenJournalstatusMottatt() {
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();
		journalpostDto.setJournalstatus(JournalStatusCode.M);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JOURNALPOST_ID), DOKUMENT_INFO_ID, VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), null);

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep5LocalCaching, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, Boolean.TRUE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter(), hasSize(1));
		journalpost.getDokumenter().forEach(dokumentInfo ->
				dokumentInfo.getDokumentvarianter().forEach(dokumentvariant -> {
					assertTrue(dokumentvariant.isSaksbehandlerHarTilgang());
				}));
	}

	@Test
	void shouldUseArkivsakTemaWhenSakstilknyttetJournalpost() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE))
				.fagomrade(FagomradeCode.AAP).build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, arkivsakCacheRequestCache);

		assertThat(journalpost.getTema(), is(Tema.STO));
		assertThat(journalpost.getTemanavn(), is(Tema.STO.getTemanavn()));
	}

	@Test
	void shouldUseJournalpostTemaWhenIkkeSakstilknyttetJournalpostIngenSaksrelasjon() {
		JournalpostDto journalpostDto = baseJournalpostDto()
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
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(null, false, FAKSYSTEM_CODE))
				.fagomrade(FagomradeCode.AAP).build();

		RequestCache arkivsakCacheRequestCache = createArkivsakCacheRequestCache();
		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, arkivsakCacheRequestCache);

		assertThat(journalpost.getTema(), is(Tema.AAP));
		assertThat(journalpost.getTemanavn(), is(Tema.AAP.getTemanavn()));
	}

	@Test
	void shouldAvsenderMottakerErLikBrukerTrueWhenBrukerIsSameAsAvsenderMottakerId() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.bruker(BrukerDto.builder().brukerId(AVSENDER_MOTTAKER_ID).build())
				.build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertTrue(journalpost.getAvsenderMottaker().isErLikBruker());
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsInngaaende() {
		RequestCache requestCache = pep5RequestCache();
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsNotat() {
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	@Test
	void shouldMapDokumenttypeIdAsBrevkodeWhenJournalpostIsUtgaaende() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(DOKUMENTTYPE_ID));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsUtgaaendeAndDokumenttypeIdNotSet() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.getDokumenter().get(0).setDokumenttypeId(null);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(BREVKODE));
	}

	// Se https://jira.adeo.no/browse/MMA-3076
	@Test
	void shouldMapFromFagomradeOKOToTemaSTO() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getTema(), is(Tema.STO));
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeNull(){
		JournalpostDto journalpostDto =  buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerId(null);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getAvsenderMottaker().getType(), is(AvsenderMottakerIdType.NULL));

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
		assertEquals(JOURNALFOERENDE_ENHET, journalpost.getJournalforendeEnhet());
		assertEquals(OPPRETTET_AV_NAVN, journalpost.getOpprettetAvNavn());
		assertEquals(SKJERMING_TYPE_CODE_POL.name(), journalpost.getSkjerming());

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
		assertEquals(SKJERMING_TYPE_CODE_POL.name(), dokumentInfo1.getSkjerming());
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getLogiskVedleggId(), is(LOGISK_VEDLEGG_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is(LOGISK_VEDLEGG_TITTEL));

		Assertions.assertThat(dokumentInfo1.getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::getFilnavn, Dokumentvariant::isSaksbehandlerHarTilgang, Dokumentvariant::getSkjerming)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), FILNAVN_1, false, SKJERMING_TYPE_CODE_POL
								.name()),
						tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), FILNAVN_2, false, null));

		assertEquals(Dokumentstatus.FERDIGSTILT, dokumentInfo1.getDokumentstatus());
		assertEquals(AKTOER_ID, journalpost.getBruker().getId());
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
						.foedselsnr(FNR)
						.aktoerId(AKTOER_ID)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCachePSAK() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(SAKS_ID + Arkivsakssystem.PSAK.name(),
				Arkivsak.builder()
						.aktoerId(AKTOER_ID)
						.arkivsaksystem(Arkivsakssystem.PSAK)
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

	private List<DokumentInfoDto> buildDokumenter() {
		return Collections.singletonList(
				DokumentInfoDto.builder()
						.dokumentInfoId(DOKUMENT_INFO_ID)
						.tittel("veldigViktigTittel")
						.brevkode(BREVKODE)
						.dokumenttypeId(DOKUMENTTYPE_ID)
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.datoFerdigstilt(DATO_FERDIGSTILT)
						.origJournalpostId(JOURNALPOST_ID)
						.skjerming(SKJERMING_TYPE_CODE_POL)
						.logiske(logiskeVedlegg())
						.varianter(Arrays.asList(VariantDto.builder()
										.skjerming(SKJERMING_TYPE_CODE_POL)
										.variantf(VARIANT_FORMAT_CODE_ARKIV)
										.filnavn(FILNAVN_1)
										.build(),
								VariantDto.builder()
										.skjerming(null)
										.variantf(VARIANT_FORMAT_CODE_SLADDET)
										.filnavn(FILNAVN_2)
										.build()))
						.build());
	}

	private List<LogiskVedleggDto> logiskeVedlegg() {
		LogiskVedleggDto logiskVedleggDto = new LogiskVedleggDto();
		logiskVedleggDto.setVedleggId(LOGISK_VEDLEGG_ID);
		logiskVedleggDto.setTittel(LOGISK_VEDLEGG_TITTEL);
		return Collections.singletonList(logiskVedleggDto);
	}

	private JournalpostDto buildJournalpostDtoUtgaaendeType(JournalStatusCode journalStatusCode) {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.U)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE))
				.journalstatus(journalStatusCode)
				.journalDato(JOURNAL_DATO)
				.dokumentDato(DOKUMENT_DATO)
				.avsReturDato(AVS_RETUR_DATO)
				.sendtPrintDato(SENDT_PRINT_DATO)
				.ekspedertDato(EKSPEDERT_DATO)
				.build();
	}

	private JournalpostDto buildJournalpostDtoInngaaendeType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE))
				.mottattDato(MOTTAT_DATO)
				.journalDato(JOURNAL_DATO)
				.build();
	}

	private JournalpostDto buildJournalpostDtoInternNotatType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE))
				.journalDato(JOURNAL_DATO)
				.build();
	}

	private JournalpostDto buildJournalpostDtoPenSaksrelasjonDto() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FagsystemCode.PEN))
				.build();
	}


	private JournalpostDto.JournalpostDtoBuilder baseJournalpostDto() {
		return JournalpostDto.builder()
				.journalpostId(JOURNALPOST_ID)
				.nextJournalpostId(405252858L)
				.innhold(INNHOLD)
				.fagomrade(FAGOMRADE)
				.behandlingstema(BEHANDLINGSTEMA)
				.behandlingstemanavn(BEHANDLINGSTEMANAVN)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerLand(AVSENDER_MOTTAKER_LAND)
				.journalforendeEnhet(JOURNALFOERENDE_ENHET)
				.journalfortAvNavn(JOURNALFOERT_AV)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.datoOpprettet(DATO_OPPRETTET)
				.journalstatus(JournalStatusCode.J)
				.skjerming(SKJERMING_TYPE_CODE_POL)
				.tilleggsopplysninger(Collections.singletonList(TilleggsopplysningDto.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.dokumenter(buildDokumenter());
	}
}