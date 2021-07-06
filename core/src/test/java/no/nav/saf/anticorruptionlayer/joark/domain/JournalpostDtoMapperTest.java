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
import no.nav.saf.domain.DomainConstants;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
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

import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper.FILTYPE_PDF;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
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
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.U.toString(), journalpost.getJournalposttype().toString());
		assertEquals(JournalStatusCode.E.toSafJournalstatus(), journalpost.getJournalstatus());
		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
		assertEquals(JournalpostDtoTestObjects.ANTALL_RETUR, journalpost.getAntallRetur());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.DOKUMENT_DATO, Datotype.DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JournalpostDtoTestObjects.MOTTAT_DATO, Datotype.DATO_REGISTRERT))));
	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.I, journalpost.getJournalposttype());
		assertEquals(Journalstatus.JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), Kanal.UKJENT.getKanalnavn());
		assertNull(journalpost.getAntallRetur());

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JournalpostDtoTestObjects.DOKUMENT_DATO, Datotype.DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JournalpostDtoTestObjects.AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JournalpostDtoTestObjects.SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JournalpostDtoTestObjects.EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.MOTTAT_DATO, Datotype.DATO_REGISTRERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
	}

	@Test
	void shouldMapJournalpostDtoWithNotatJournalpost() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, true);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(Journalposttype.N, journalpost.getJournalposttype());
		assertEquals(Journalstatus.JOURNALFOERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), Kanal.INGEN_DISTRIBUSJON.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JournalpostDtoTestObjects.JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));
	}

	@Test
	void shouldNotMapDokumentinfoIngenTilgangPep5() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		RequestCache requestCache = createTilgangBrukerRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, false);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertThat(journalpost, is(notNullValue()));
		assertThat(journalpost.getJournalpostId(), is(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID)));
		assertTrue(journalpost.getDokumenter().isEmpty());
	}

	@Test
	void shouldMapJournalpostIngenDistribusjon() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.INGEN_DISTRIBUSJON.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgSentral() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.FS);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgLokal() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.FL);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Kanal.LOKAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}


	@Test
	void shouldMapJournalpostPensjonFagsystem() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoPenSaksrelasjonDto();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(JournalpostDtoTestObjects.ARKIVSAK_NR, journalpost.getSak().getArkivsaksnummer());
		assertEquals(Arkivsakssystem.PSAK.name(), journalpost.getSak().getArkivsaksystem().name());
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
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = createTilgangBrukerCacheWithOrganisasjonSak();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(JournalpostDtoTestObjects.ORG_NR, journalpost.getBruker().getId());
	}

	@Test
	void shouldMapJournalpostWithKasserDokument() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.getDokumenter().get(0).setKassert(true);
		RequestCache requestCache = pep5RequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Dokumentstatus.KASSERT, journalpost.getDokumenter().get(0).getDokumentstatus());
	}

	@Test
	void shouldMapJournalpostWithNullDokumentStatus() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
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

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);

		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());

		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
						.name(), JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());


		RequestCache requestCache = new RequestCache();
		requestCache.putObject(tilgangKeyPep2dLocalCaching, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep5LocalCaching, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantArkiv, Boolean.TRUE);
		requestCache.putObject(tilgangKeyPep6dLocalCachingVariantSladdet, Boolean.FALSE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		Assertions.assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), true));
	}

	@Test
	void shouldHaveSaksbehandlerHaveTilgangTrueWhenJournalstatusMottatt() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setJournalstatus(JournalStatusCode.M);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
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
	void shouldHaveSaksbehandlerHaveTilgangTrueWhenJournalstatusUtgaarAndIsMidlertidigIngenSak() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setJournalstatus(JournalStatusCode.U);
		journalpostDto.setSaksrelasjon(null);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
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
	void shouldHaveSaksbehandlerHaveTilgangTrueWhenTemaUkjent() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setFagomrade(FagomradeCode.UKJ);

		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		String tilgangKeyPep6dLocalCachingVariantArkiv = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
						.name(), JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL.getSafSkjerming().name());
		String tilgangKeyPep6dLocalCachingVariantSladdet = KeyGeneratorLocalCaching.getKeyForPep6d(
				String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID, JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
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
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(JournalpostDtoTestObjects.SAKS_ID, false, JournalpostDtoTestObjects.FAKSYSTEM_CODE, null,
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
				.saksrelasjon(new SaksrelasjonDto(null, false, JournalpostDtoTestObjects.FAKSYSTEM_CODE, null, null,
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

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(JournalpostDtoTestObjects.BREVKODE));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsNotat() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(JournalpostDtoTestObjects.BREVKODE));
	}

	@Test
	void shouldMapDokumenttypeIdAsBrevkodeWhenJournalpostIsUtgaaende() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(JournalpostDtoTestObjects.DOKUMENTTYPE_ID));
	}

	@Test
	void shouldMapBrevkodeAsBrevkodeWhenJournalpostIsUtgaaendeAndDokumenttypeIdNotSet() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.getDokumenter().get(0).setDokumenttypeId(null);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getDokumenter().get(0).getBrevkode(), is(JournalpostDtoTestObjects.BREVKODE));
	}

	// Se https://jira.adeo.no/browse/MMA-3076
	@Test
	void shouldMapFromFagomradeOKOToTemaSTO() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, pep5RequestCache());

		assertThat(journalpost.getTema(), is(Tema.STO));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForPerson() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(JournalpostDtoTestObjects.BRUKER_ID_PERSON)
						.brukerIdType(DomainConstants.PERSON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(JournalpostDtoTestObjects.BRUKER_ID_PERSON));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.FNR));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjon() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(JournalpostDtoTestObjects.BRUKER_ID_ORGANISASJON)
						.brukerIdType(DomainConstants.ORGANISASJON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(JournalpostDtoTestObjects.BRUKER_ID_ORGANISASJON));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.ORGNR));
	}

	@Test
	void shouldMapToBrukerInJournalpostWhenNoSakstilknytningForOrganisasjonAndTrailingBlanks() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(null)
				.bruker(BrukerDto.builder()
						.brukerId(JournalpostDtoTestObjects.BRUKER_ID_ORGANISASJON + "   ")
						.brukerIdType(DomainConstants.ORGANISASJON)
						.build())
				.fagomrade(FagomradeCode.AAP).build();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, new RequestCache());

		assertThat(journalpost.getBruker().getId(), is(JournalpostDtoTestObjects.BRUKER_ID_ORGANISASJON));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.ORGNR));
	}

	@Test
	void shouldMapJournalstatusFeilregistrertWhenJournalfoertAndIsFeilregistrert() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.J);
		journalpostDto.getSaksrelasjon().setFeilregistrert(true);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Journalstatus.FEILREGISTRERT, journalpost.getJournalstatus());
	}

	@Test
	void shouldMapJournalstatusUtgaarWhenUtgaarAndIsFeilregistrert() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType(JournalStatusCode.U);
		journalpostDto.getSaksrelasjon().setFeilregistrert(true);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Journalstatus.UTGAAR, journalpost.getJournalstatus());
	}

	private RequestCache pep5RequestCache() {
		RequestCache requestCache = createArkivsakCacheRequestCache();
		String tilgangKeyPep5LocalCaching = KeyGeneratorLocalCaching.getKeyForPep5(String.valueOf(JournalpostDtoTestObjects.JOURNALPOST_ID), JournalpostDtoTestObjects.DOKUMENT_INFO_ID);
		requestCache.putObject(tilgangKeyPep5LocalCaching, true);
		return requestCache;
	}

	private void assertCommonMetadata(Journalpost journalpost) {
		assertEquals(Long.toString(JournalpostDtoTestObjects.JOURNALPOST_ID), journalpost.getJournalpostId());
		assertEquals(JournalpostDtoTestObjects.INNHOLD, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafTema(JournalpostDtoTestObjects.FAGOMRADE), journalpost.getTema());
		assertEquals(JournalpostDtoTestObjects.JOURNALFOERT_AV, journalpost.getJournalfortAvNavn());
		assertEquals(JournalpostDtoTestObjects.BEHANDLINGSTEMA, journalpost.getBehandlingstema());
		assertEquals(JournalpostDtoTestObjects.BEHANDLINGSTEMANAVN, journalpost.getBehandlingstemanavn());
		assertThat(journalpost.getAvsenderMottaker().getId(), is(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID));
		assertThat(journalpost.getAvsenderMottaker().getType(), is(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID_TYPE));
		assertThat(journalpost.getAvsenderMottaker().getNavn(), is(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_NAVN));
		assertThat(journalpost.getAvsenderMottaker().getLand(), is(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_LAND));
		assertFalse(journalpost.getAvsenderMottaker().isErLikBruker());
		assertEquals(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottakerId());
		assertEquals(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_NAVN, journalpost.getAvsenderMottakerNavn());
		assertEquals(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_LAND, journalpost.getAvsenderMottakerLand());
		assertEquals(JournalpostDtoTestObjects.JOURNALFOERENDE_ENHET, journalpost.getJournalforendeEnhet());
		assertEquals(JournalpostDtoTestObjects.OPPRETTET_AV_NAVN, journalpost.getOpprettetAvNavn());
		assertEquals(Skjerming.POL, journalpost.getSkjerming());

		assertEquals(LocalDateTime.from(JournalpostDtoTestObjects.DATO_OPPRETTET.toInstant()
				.atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));
		assertEquals(1, journalpost.getDokumenter().size());
		assertThat(journalpost.getTilleggsopplysninger(), hasSize(1));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel(), is(JournalpostDtoTestObjects.TILLEGGSOPPLYSNING_NOKKEL));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi(), is(JournalpostDtoTestObjects.TILLEGGSOPPLYSNING_VERDI));

		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertEquals(JournalpostDtoTestObjects.DOKUMENT_INFO_ID, dokumentInfo1.getDokumentInfoId());
		assertThat(dokumentInfo1.getDatoFerdigstilt(), equalTo(LocalDateTime.from(JournalpostDtoTestObjects.DATO_FERDIGSTILT.toInstant()
				.atZone(ZoneId.systemDefault()))));
		assertEquals(Long.toString(JournalpostDtoTestObjects.JOURNALPOST_ID), dokumentInfo1.getOriginalJournalpostId());
		assertEquals(Skjerming.POL, dokumentInfo1.getSkjerming());
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getLogiskVedleggId(), is(JournalpostDtoTestObjects.LOGISK_VEDLEGG_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is(JournalpostDtoTestObjects.LOGISK_VEDLEGG_TITTEL));

		Assertions.assertThat(dokumentInfo1.getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFilnavn,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::isSaksbehandlerHarTilgang,
						Dokumentvariant::getSkjerming,
						Dokumentvariant::getFiltype)
				.hasSize(2)
				.containsExactlyInAnyOrder(
						tuple(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(),
								JournalpostDtoTestObjects.FILNAVN_1,
								JournalpostDtoTestObjects.FILUUID_1, false,
								Skjerming.POL,
								FILTYPE_PDF),
						tuple(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(),
								JournalpostDtoTestObjects.FILNAVN_2,
								JournalpostDtoTestObjects.FILUUID_2,
								false,
								null,
								FILTYPE_PDF));

		assertEquals(Dokumentstatus.FERDIGSTILT, dokumentInfo1.getDokumentstatus());
		assertEquals(JournalpostDtoTestObjects.AKTOER_ID, journalpost.getBruker().getId());
		assertEquals(JournalpostDtoTestObjects.KANAL_REFERANSE_ID, journalpost.getEksternReferanseId());
	}

	private RequestCache createArkivsakCacheRequestCache() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(JournalpostDtoTestObjects.SAKS_ID + Arkivsakssystem.GSAK.name(),
				Arkivsak.builder()
						.aktoerId(JournalpostDtoTestObjects.AKTOER_ID)
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
						.aktoerId(JournalpostDtoTestObjects.AKTOER_ID)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCachePSAK() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(JournalpostDtoTestObjects.SAKS_ID + Arkivsakssystem.PSAK.name(),
				Arkivsak.builder()
						.aktoerId(JournalpostDtoTestObjects.AKTOER_ID)
						.arkivsaksystem(Arkivsakssystem.PSAK)
						.arkivsaksnummer(JournalpostDtoTestObjects.ARKIVSAK_NR)
						.tema(Tema.PEN)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerCacheWithOrganisasjonSak() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(TILGANG_BRUKER,
				TilgangBruker.builder()
						.orgnummer(JournalpostDtoTestObjects.ORG_NR)
						.build()
		);
		return requestCache;
	}
}