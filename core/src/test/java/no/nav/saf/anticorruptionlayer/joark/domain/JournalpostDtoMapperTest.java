package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.LogiskVedleggDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.VariantDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.SafRequestContext;
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

	private static final String OIDC_TOKEN_PERSON = "Bearer " + "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJvNFUwMVhKNmlnRmw0VGYwdFRkYjR3IiwgInN1YiI6ICJaOTkwNDI0IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJlYTdmNWUxMi1jYjZjLTQ1ZjUtYmViMi0wYjVkYmI5ZDQ3YTItMTMzNzkzNCIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJpZGEtdCIsICJjX2hhc2giOiAiRnJwNzhwdlJZU0VPMExjUktPUFdWdyIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjJjYjQ2OGU4LThmMjItNGY1NS1hYTQ4LWM1NWExYjA4YmQ1ZiIsICJhenAiOiAiaWRhLXQiLCAiYXV0aF90aW1lIjogMTU0MzU3Nzk3MiwgInJlYWxtIjogIi8iLCAiZXhwIjogMTU0MzU4MTU3MiwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTQzNTc3OTcyIH0.NRgKaZhZ7qbBbJMUj_l9kzGOv7yOJVRVZDqmK0-G9lxzZs4jW1AtvFWqJRO9dd_djlIOGXz93UnuMNpWYWuoUd_S9gVc53yUjquzrys1IK8Zjd89smEl_9QP3ya8z7ISv48DciJORxdB2XT8rr2qpltYjKrCE2QmmK2ctAhy9QuFwEoZnctrR8IDKhUJCGd8LXPXddNRNEDL4-A47KwkF0UcfoDzPXznyZ2cbV4IkT3zvGqqwO3hovdrpadBdf204hClcmETYN3frRh1qHuTUqrBL7ualfqs-eDa4FKd77Mwu02LqPQGVpt8Ebebtv3OlS28YDchx8ng_P05okSjZg";

	private static final String DOKUMENT_INFO_ID = "1234";
	private static final VariantFormatCode VARIANT_FORMAT_CODE_ARKIV = VariantFormatCode.ARKIV;
	private static final VariantFormatCode VARIANT_FORMAT_CODE_SLADDET = VariantFormatCode.SLADDET;
	private static final SkjermingTypeCode SKJERMING_TYPE_CODE_POL = SkjermingTypeCode.POL;
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

	@Mock
	private OidcValidatorTool oidcValidatorTool;

	@Test
	void shouldMapJournalpostDtoWithUtgaaendeJournalpost() {

		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = createArkivsakCacheRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.U.toString(), journalpost.getJournalposttype().toString());
		assertEquals(JournalStatusCode.E.toSafJournalstatus(), journalpost.getJournalstatus());
		assertEquals(SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT))));
	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {

		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertCommonMetadata(journalpost);

		assertEquals(JournalpostTypeCode.I.toString(), journalpost.getJournalposttype().toString());
		assertEquals(Journalstatus.FEILREGISTRERT, journalpost.getJournalstatus());
		assertEquals(journalpost.getKanalnavn(), Kanal.UKJENT.getKanalnavn());

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT)));

	}

	@Test
	void shouldMapJournalpostIngenDistribusjon() {
		JournalpostDto journalpostDto = buildJournalpostDtoInternNotatType();
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertEquals(Kanal.INGEN_DISTRIBUSJON.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgSentral() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FS);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertEquals(Kanal.SENTRAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}

	@Test
	void shouldMapJournalpostManglendeUtsendingskanalFerdigOgLokal() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.FL);
		RequestCache requestCache = createTilgangBrukerRequestCache();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertEquals(Kanal.LOKAL_UTSKRIFT.getKanalnavn(), journalpost.getKanalnavn());
	}


	@Test
	void shouldMapJournalpostPensjonFagsystem() {
		JournalpostDto journalpostDto = buildJournalpostDtoPenSaksrelasjonDto();
		RequestCache requestCache = createTilgangBrukerRequestCachePSAK();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertEquals(ARKIVSAK_NR, journalpost.getSak().getArkivsaksnummer());
		assertEquals(Arkivsakssystem.PSAK.name(), journalpost.getSak().getArkivsaksystem().name());
	}

	@Test
	void shouldMapJournalpostOrgNr() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		RequestCache requestCache = createTilgangBrukerCacheWithOrganisasjonSak();

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache, null);

		assertEquals(ORG_NR, journalpost.getBruker().getId());
	}

	@Test
	void shouldMapSaksbehandlerHarTilgang() {

		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON)).thenReturn(true);
		SafRequestContext safRequestContext = new SafRequestContext(OIDC_TOKEN_PERSON, oidcValidatorTool);
		JournalpostDto journalpostDto = buildJournalpostDtoPenSaksrelasjonDto();

		String tilgangKeyPep2d = "tilgang:" + safRequestContext.getSecurityContext()
				.getSaksbehandlerId() + ":tema=" + journalpostDto.getFagomrade();

		String tilgangKeyPep6VariantArkiv = "tilgang:" + safRequestContext.getSecurityContext()
				.getSaksbehandlerId() + ":ressurstype:dokument_fil" + ":variantformat:" + VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat()
				+ ":skjerming=" + SKJERMING_TYPE_CODE_POL;

		String tilgangKeyPep6VariantSladdet = "tilgang:" + safRequestContext.getSecurityContext()
				.getSaksbehandlerId() + ":ressurstype:dokument_fil" + ":variantformat:" + VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat()
				+ ":skjerming=" + null;


		safRequestContext.getRequestCache().putObject(tilgangKeyPep2d, Boolean.TRUE);
		safRequestContext.getRequestCache().putObject(tilgangKeyPep6VariantArkiv, Boolean.TRUE);
		safRequestContext.getRequestCache().putObject(tilgangKeyPep6VariantSladdet, Boolean.FALSE);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache(), safRequestContext
				.getSecurityContext());

		Assertions.assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), true));

	}

	private void assertCommonMetadata(Journalpost journalpost) {
		assertEquals(Long.toString(journalpostId), journalpost.getJournalpostId());
		assertEquals(INNHOLD, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafTema(FAGOMRADE), journalpost.getTema());
		assertEquals(JOURNALFOERT_AV, journalpost.getJournalfortAvNavn());

		assertEquals(LocalDateTime.from(DATO_OPPRETTET.toInstant()
				.atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));
		assertEquals(1, journalpost.getDokumenter().size());
		assertEquals(DOKUMENT_INFO_ID, journalpost.getDokumenter().get(0).getDokumentInfoId());

		Assertions.assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.hasSize(2)
				.containsExactlyInAnyOrder(tuple(VARIANT_FORMAT_CODE_SLADDET.getSafVariantformat(), false),
						tuple(VARIANT_FORMAT_CODE_ARKIV.getSafVariantformat(), false));

		assertEquals(BREVKODE, journalpost.getDokumenter().get(0).getBrevkode());
		assertEquals(BREVKODE, journalpost.getDokumenter().get(0).getBrevkode());
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
						.brevkode(BREVKODE)
						.logiske(Collections.singletonList(new LogiskVedleggDto()))
						.varianter(Arrays.asList(VariantDto.builder()
										.skjerming(SKJERMING_TYPE_CODE_POL)
										.variantFormat(VARIANT_FORMAT_CODE_ARKIV)
										.build(),
								VariantDto.builder()
										.skjerming(null)
										.variantFormat(VARIANT_FORMAT_CODE_SLADDET)
										.build()))
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