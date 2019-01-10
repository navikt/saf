package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.tjeneste.visningsmodell.kode.Kanal.SENTRAL_UTSKRIFT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.LogiskVedleggDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.RelevantDato;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class JournalpostDtoMapperTest {

	private static final String DOKUMENT_INFO_ID = "1234";
	private static final VariantFormatCode VARIANT_FORMAT_CODE = VariantFormatCode.ARKIV;
	private static final String BREVKODE = "brevkodeX";

	private static final long journalpostId = 417457822L;
	private static final String INNHOLD = "MASKERT_FELT";
	private static final Date DATO_OPPRETTET = new Date(1000L);
	private static final Date AVS_RETUR_DATO = new Date(2000L);
	private static final Date SENDT_PRINT_DATO = new Date(3000L);
	private static final Date EKSPEDERT_DATO = new Date(4000L);
	private static final Date DOKUMENT_DATO = new Date(5000L);
	private static final Date JOURNAL_DATO = new Date(6000L);
	private static final Date MOTTAT_DATO = new Date(7000L);
	private static final String FNR = "***gammelt_fnr***";
	private static final String AKTOER_ID = "***gammelt_fnr***31";
	private static final String SAKS_ID = "12345";
	private static final String ORG_NR = "54321";
	private static final String ARKIVSAK_NR = "1337";
	private static final FagsystemCode FAKSYSTEM_CODE = FagsystemCode.FS22;

	private static final FagomradeCode FAGOMRADE = FagomradeCode.STO;
	private static final String JOURNALFOERT_AV = "Automatisk jobb";

	private final JournalpostDtoMapper mapper = new JournalpostDtoMapper();

	@Test
	void shouldMapJournalpostDtoWithUtgaaendeJournalpost() {

		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = createArkivsakCacheRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.U.toString(), journalpost.getJournalposttype().toString());
		assertEquals(JournalStatusCode.E.toSafJournalStatus(), journalpost.getJournalstatus());
		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_MOTTATT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT))));
	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {

		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.I.toString(), journalpost.getJournalposttype().toString());
		assertEquals(Journalstatus.FEILREGISTRERT, journalpost.getJournalstatus());
		assertNull(journalpost.getKanalnavn()); // TODO, replace with Kanal.UKJENT kode when done.

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_MOTTATT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));

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

	private void assertCommonMetadata(Journalpost journalpost) {
		assertEquals(Long.toString(journalpostId), journalpost.getJournalpostId());
		assertEquals(INNHOLD, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafJournalstatus(FAGOMRADE), journalpost.getTema());
		assertEquals(JOURNALFOERT_AV, journalpost.getJournalfortAvNavn());

		assertEquals(LocalDateTime.from(DATO_OPPRETTET.toInstant().atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));
		assertEquals(1, journalpost.getDokumenter().size());
		assertEquals(DOKUMENT_INFO_ID, journalpost.getDokumenter().get(0).getDokumentInfoId());
		assertEquals(VARIANT_FORMAT_CODE.toString(), journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getVariantformat().toString());
		assertEquals(BREVKODE, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(BREVKODE, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(AKTOER_ID, journalpost.getBruker().getId());
	}


	private RequestCache createArkivsakCacheRequestCache() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(TILGANG_BRUKER,
				TilgangBruker.builder()
						.foedselsnr(FNR)
						.aktoerId(AKTOER_ID)
						.build()
		);
		return requestCache;
	}

	private RequestCache createTilgangBrukerRequestCache() {
		RequestCache requestCache = new RequestCache();
		requestCache.putObject(SAKS_ID + Arkivsakssystem.GSAK.name(),
				Arkivsak.builder()
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
						.variantFormat(VARIANT_FORMAT_CODE)
						.brevkode(BREVKODE)
						.logiske(Collections.singletonList(new LogiskVedleggDto()))
						.build());
	}

	private JournalpostDto buildJournalpostDtoUtgaaendeType(JournalStatusCode journalStatusCode) {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.U)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE))
				.journalstatus(journalStatusCode)
				.dokumentDato(DOKUMENT_DATO)
				.avsReturDato(AVS_RETUR_DATO)
				.sendtPrintDato(SENDT_PRINT_DATO)
				.ekspedertDato(EKSPEDERT_DATO)
				.build();
	}

	private JournalpostDto buildJournalpostDtoInngaaendeType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, true, FAKSYSTEM_CODE))
				.mottattDato(MOTTAT_DATO)
				.journalDato(JOURNAL_DATO)
				.build();
	}

	private JournalpostDto buildJournalpostDtoInternNotatType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, true, FAKSYSTEM_CODE))
				.build();
	}

	private JournalpostDto buildJournalpostDtoPenSaksrelasjonDto() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, true, FagsystemCode.PEN))
				.build();
	}

	private JournalpostDto.JournalpostDtoBuilder baseJournalpostDto() {
		return JournalpostDto.builder()
				.journalpostId(journalpostId)
				.nextJournalpostId(405252858L)
				.innhold(INNHOLD)
				.fagomrade(FAGOMRADE)
				.journalfortAvNavn(JOURNALFOERT_AV)
				.datoOpprettet(DATO_OPPRETTET)
				.dokumenter(buildDokumenter());
	}

}