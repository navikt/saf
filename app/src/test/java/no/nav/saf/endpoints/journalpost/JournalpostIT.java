package no.nav.saf.endpoints.journalpost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import no.nav.saf.endpoints.graphql.GraphQLResponse;
import no.nav.saf.graphql.ErrorCode;
import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer.SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.FAGSAK;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.GENERELL_SAK;
import static no.nav.saf.domain.kode.Datotype.DATO_DOKUMENT;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Datotype.DATO_JOURNALFOERT;
import static no.nav.saf.domain.kode.Datotype.DATO_REGISTRERT;
import static no.nav.saf.domain.kode.Dokumentstatus.FERDIGSTILT;
import static no.nav.saf.domain.kode.Journalstatus.MOTTATT;
import static no.nav.saf.domain.kode.Kanal.SDP;
import static no.nav.saf.domain.kode.Kanal.SKAN_IM;
import static no.nav.saf.domain.kode.Tema.HJE;
import static no.nav.saf.domain.kode.Tema.UFO;
import static no.nav.saf.domain.kode.Variantformat.ARKIV;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.AKTOERID;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.ORGNR;
import static no.nav.saf.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.saf.graphql.ErrorCode.FORBIDDEN;
import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.JOURNALSTATUS;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.ORGNR_NAV_STAT;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.TEMA;
import static no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode.UKJENT;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP1G_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP2_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP3_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP4_DENY_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

class JournalpostIT extends AbstractItest {
	private static final String JOURNALPOST_ID = "400000000";
	private static final String EKSTERNREFERANSE_ID = "cd047c37-aaaf-4dda-83a3773ed636f452";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	@BeforeEach
	void setUp() {
		setupHappyPathAzureToken();
	}

	@Test
	void shouldQueryInngaaendeJournalpostByJournalpostIdWhenAllAccessPermit() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());

		assertInngaaendeJournalpost(journalpost);
		assertInngaaendeDokumenter(journalpost);
	}

	private static void assertInngaaendeJournalpost(Journalpost journalpost) {
		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getTittel()).isEqualTo("NAV 10-07.34 Tilskudd ved kjøp av briller til barn");
		assertThat(journalpost.getJournalposttype()).isEqualTo(Journalposttype.I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(Journalstatus.JOURNALFOERT);
		assertThat(journalpost.getTema()).isEqualTo(Tema.HJE);
		assertThat(journalpost.getTemanavn()).isEqualTo(Tema.HJE.getTemanavn());
		assertThat(journalpost.getBehandlingstema()).isEqualTo("ab0071");
		assertThat(journalpost.getBehandlingstemanavn()).isEqualTo("Tilskudd");
		assertThat(journalpost.getSak().getArkivsaksnummer()).isEqualTo("140000000");
		assertThat(journalpost.getSak().getArkivsaksystem()).isEqualTo(Arkivsakssystem.GSAK);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo("2514");
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo("HJELPEMIDLER");
		assertThat(journalpost.getSak().getDatoOpprettet()).isEqualTo(LocalDateTime.parse("2023-09-08T14:16:28"));
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(Sakstype.FAGSAK);
		assertThat(journalpost.getSak().getTema()).isEqualTo(Tema.HJE);
		assertThat(journalpost.getBruker().getId()).isEqualTo(AKTOER_ID);
		assertThat(journalpost.getBruker().getType()).isEqualTo(AKTOERID);
		assertThat(journalpost.getAvsenderMottaker().getId()).isEqualTo("07480966982");
		assertThat(journalpost.getAvsenderMottaker().getType()).isEqualTo(AvsenderMottakerIdType.FNR);
		assertThat(journalpost.getAvsenderMottaker().getNavn()).isEqualTo("Sitrongul Ovn");
		assertThat(journalpost.getAvsenderMottaker().getLand()).isEqualTo("NO");
		assertThat(journalpost.getAvsenderMottaker().isErLikBruker()).isTrue();
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo("07480966982");
		assertThat(journalpost.getAvsenderMottakerNavn()).isEqualTo("Sitrongul Ovn");
		assertThat(journalpost.getAvsenderMottakerLand()).isEqualTo("NO");
		assertThat(journalpost.getAntallRetur()).isNull();
		assertThat(journalpost.getJournalforendeEnhet()).isEqualTo("4710");
		assertThat(journalpost.getJournalfoerendeEnhet()).isEqualTo("4710");
		assertThat(journalpost.getJournalfortAvNavn()).isEqualTo("teamdigihot:hm-joark-sink");
		assertThat(journalpost.getOpprettetAvNavn()).isEqualTo("Max Mekker");
		assertThat(journalpost.getKanal()).isEqualTo(SKAN_IM);
		assertThat(journalpost.getKanalnavn()).isEqualTo(SKAN_IM.getKanalnavn());
		assertThat(journalpost.getDatoOpprettet()).isEqualTo(LocalDateTime.parse("2023-08-16T13:15:00"));
		assertThat(journalpost.getRelevanteDatoer().get(0).getDato()).isEqualTo(LocalDateTime.parse("2023-08-16T13:15:00"));
		assertThat(journalpost.getRelevanteDatoer().get(0).getDatotype()).isEqualTo(DATO_DOKUMENT);
		assertThat(journalpost.getRelevanteDatoer().get(1).getDato()).isEqualTo(LocalDateTime.parse("2023-09-12T15:42:13"));
		assertThat(journalpost.getRelevanteDatoer().get(1).getDatotype()).isEqualTo(DATO_JOURNALFOERT);
		assertThat(journalpost.getRelevanteDatoer().get(2).getDato()).isEqualTo(LocalDateTime.parse("2023-08-16T13:15:00"));
		assertThat(journalpost.getRelevanteDatoer().get(2).getDatotype()).isEqualTo(DATO_REGISTRERT);
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel()).isEqualTo("brilletype");
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi()).isEqualTo("trippel-brille");
		assertThat(journalpost.getEksternReferanseId()).isEqualTo("d35c8412-7b98-4a66-8fdd-51f44ed6c632HJE-DIGITAL-SOKNAD");
		assertThat(journalpost.getUtsendingsinfo()).isNull();
	}

	private static void assertInngaaendeDokumenter(Journalpost journalpost) {
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo1.getDokumentInfoId()).isEqualTo("500000000");
		assertThat(dokumentInfo1.getTittel()).isEqualTo("NAV 10-07.34 Tilskudd ved kjøp av briller til barn");
		assertThat(dokumentInfo1.getBrevkode()).isEqualTo("NAV 10-07.34");
		assertThat(dokumentInfo1.getDokumentstatus()).isEqualTo(FERDIGSTILT);
		assertThat(dokumentInfo1.getOriginalJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getLogiskVedleggId()).isEqualTo("300000000");
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel()).isEqualTo("Skjema");
		assertThat(dokumentInfo1.getDokumentvarianter()).hasSize(1);
		Dokumentvariant dokumentvariant = dokumentInfo1.getDokumentvarianter().get(0);
		assertThat(dokumentvariant.getVariantformat()).isEqualTo(ARKIV);
		assertThat(dokumentvariant.isSaksbehandlerHarTilgang()).isTrue();
		assertThat(dokumentvariant.getFiltype()).isEqualTo("PDF");
		assertThat(dokumentvariant.getFilnavn()).isEqualTo("tilskudd.pdf");
		assertThat(dokumentvariant.getFiluuid()).isEqualTo("4b4d0d13-5c8c-4f6b-922c-4026f1679069");
		assertThat(dokumentvariant.getFilstoerrelse()).isEqualTo(4721);
	}

	@Test
	void shouldQueryUtgaaendeJournalpostByJournalpostIdWhenAllAccessPermit() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-utgaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());

		assertUtgaaendeJournalpost(journalpost);
	}

	@Test
	void shouldQueryUtgaaendeJournalpostByEksternReferanseIdWhenAllAccessPermit() {
		abacPermit();
		stubDokarkivJournalpostEksternReferanseId("journalpost-gsak-utgaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery("journalpost_eksternreferanse_id.query"));

		assertUtgaaendeJournalpost(journalpost);
	}

	private static void assertUtgaaendeJournalpost(Journalpost journalpost) {
		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getTittel()).isEqualTo("Vedtak om revurdert overgangstønad");
		assertThat(journalpost.getJournalposttype()).isEqualTo(Journalposttype.U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(Journalstatus.EKSPEDERT);
		assertThat(journalpost.getTema()).isEqualTo(Tema.ENF);
		assertThat(journalpost.getTemanavn()).isEqualTo(Tema.ENF.getTemanavn());
		assertThat(journalpost.getBehandlingstema()).isEqualTo("ab0071");
		assertThat(journalpost.getBehandlingstemanavn()).isEqualTo("Overgangsstønad");
		assertThat(journalpost.getSak().getArkivsaksnummer()).isEqualTo("140000000");
		assertThat(journalpost.getSak().getArkivsaksystem()).isEqualTo(Arkivsakssystem.GSAK);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo("200054151");
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo("EF");
		assertThat(journalpost.getSak().getDatoOpprettet()).isEqualTo(LocalDateTime.parse("2023-11-21T14:21:12"));
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(Sakstype.FAGSAK);
		assertThat(journalpost.getSak().getTema()).isEqualTo(Tema.ENF);
		assertThat(journalpost.getBruker().getId()).isEqualTo(AKTOER_ID);
		assertThat(journalpost.getBruker().getType()).isEqualTo(AKTOERID);
		assertThat(journalpost.getAvsenderMottaker().getId()).isEqualTo("23496940474");
		assertThat(journalpost.getAvsenderMottaker().getType()).isEqualTo(AvsenderMottakerIdType.FNR);
		assertThat(journalpost.getAvsenderMottaker().getNavn()).isEqualTo("SNÅL LOGARITME");
		assertThat(journalpost.getAvsenderMottaker().getLand()).isNull();
		assertThat(journalpost.getAvsenderMottaker().isErLikBruker()).isTrue();
		assertThat(journalpost.getAvsenderMottakerId()).isEqualTo("23496940474");
		assertThat(journalpost.getAvsenderMottakerNavn()).isEqualTo("SNÅL LOGARITME");
		assertThat(journalpost.getAvsenderMottakerLand()).isNull();
		assertThat(journalpost.getAntallRetur()).isEqualTo("1");
		assertThat(journalpost.getJournalforendeEnhet()).isEqualTo("4489");
		assertThat(journalpost.getJournalfoerendeEnhet()).isEqualTo("4489");
		assertThat(journalpost.getJournalfortAvNavn()).isEqualTo("Bjarne Betjent");
		assertThat(journalpost.getOpprettetAvNavn()).isEqualTo("Bjarne Betjent");
		assertThat(journalpost.getKanal()).isEqualTo(SDP);
		assertThat(journalpost.getKanalnavn()).isEqualTo(SDP.getKanalnavn());
		assertThat(journalpost.getDatoOpprettet()).isEqualTo(LocalDateTime.parse("2023-11-21T16:19:35"));
		assertThat(journalpost.getRelevanteDatoer().get(0).getDato()).isEqualTo(LocalDateTime.parse("2023-11-21T16:19:35"));
		assertThat(journalpost.getRelevanteDatoer().get(0).getDatotype()).isEqualTo(DATO_DOKUMENT);
		assertThat(journalpost.getRelevanteDatoer().get(1).getDato()).isEqualTo(LocalDateTime.parse("2023-11-21T16:19:35"));
		assertThat(journalpost.getRelevanteDatoer().get(1).getDatotype()).isEqualTo(DATO_JOURNALFOERT);
		assertThat(journalpost.getRelevanteDatoer().get(2).getDato()).isEqualTo(LocalDateTime.parse("2023-11-21T16:19:37"));
		assertThat(journalpost.getRelevanteDatoer().get(2).getDatotype()).isEqualTo(DATO_EKSPEDERT);
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel()).isEqualTo("dokdistBestillingsId");
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi()).isEqualTo("6b040820-a915-4728-979f-d174cd3a42d4");
		assertThat(journalpost.getEksternReferanseId()).isEqualTo("712a2b03-adb0-44e8-be07-34e5fadbc821-vedtaksbrev");
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo1.getDokumentInfoId()).isEqualTo("500000000");
		assertThat(dokumentInfo1.getTittel()).isEqualTo("Vedtak om revurdert overgangstønad");
		assertThat(dokumentInfo1.getBrevkode()).isEqualTo("ENF_BREV_OVERGANGSSTØNAD_VEDTAK");
		assertThat(dokumentInfo1.getDokumentstatus()).isEqualTo(FERDIGSTILT);
		assertThat(dokumentInfo1.getOriginalJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(dokumentInfo1.getLogiskeVedlegg()).isEmpty();
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat()).isEqualTo(ARKIV);
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang()).isTrue();
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiltype()).isEqualTo("PDF");
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFilnavn()).isNull();
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiluuid()).isEqualTo("4ceaea4f-b01f-42fa-9992-d01e56f58962");
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFilstoerrelse()).isEqualTo(22507);
		Utsendingsinfo utsendingsInfo = journalpost.getUtsendingsinfo();
		assertThat(utsendingsInfo.getDigitalpostSendt().getAdresse()).isEqualTo("sitrongul.ovn#ABCD");
		assertThat(utsendingsInfo.getVarselSendt())
				.hasSize(2)
				.extracting(Utsendingsinfo.VarselSendt::getType,
						Utsendingsinfo.VarselSendt::getTittel,
						Utsendingsinfo.VarselSendt::getVarslingstekst,
						Utsendingsinfo.VarselSendt::getAdresse,
						Utsendingsinfo.VarselSendt::getVarslingstidspunkt)
				.containsExactlyInAnyOrder(
						tuple("EPOST", "Varsel om post", "Du har fått et vedtak fra NAV. Les det i din digitale postkasse.", "enhetstest_att_nav.no", LocalDateTime.parse("2023-11-10T10:48:14")),
						tuple("SMS", null, "Du har fått et vedtak fra NAV. Les det i din digitale postkasse.", "+47NNNNNNNNN", LocalDateTime.parse("2023-11-10T10:48:14")));
		assertThat(utsendingsInfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsInfo.getFysiskpostSendt()).isNull();
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenJournalpostIdOgEksternReferanseIdAreGiven() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery("journalpost_with_journalpostid_eksternreferanseid.query"));

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
	}

	@Test
	void shouldReturnNullWhenJournalpostIdOgEksternReferanseIdNotGiven() {
		abacPermit();

		GraphQLResponse.Error error = parseJournalpostQueryError(journalpostQuery("journalpost_with_null_journalpostid_og_eksternreferanseid.query"));
		assertThat(error.getMessage()).contains("Invalid syntax with offending token");
	}

	@Test
	void shouldQueryJournalpostWhenGenerellSakAndNoBruker() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-generell-sak.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getTema()).isEqualTo(HJE);
		assertThat(journalpost.getSak().getArkivsaksnummer()).isEqualTo("140000000");
		assertThat(journalpost.getSak().getFagsakId()).isNull();
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo("FS22");
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(GENERELL_SAK);
		assertThat(journalpost.getSak().getTema()).isEqualTo(HJE);
		assertThat(journalpost.getBruker().getId()).isEqualTo(AKTOER_ID);
		assertThat(journalpost.getBruker().getType()).isEqualTo(AKTOERID);
	}

	@Test
	void shouldQueryJournalpostWhenMidlertidig() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-midlertidig.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getTema()).isEqualTo(HJE);
		assertThat(journalpost.getSak()).isNull();
		assertThat(journalpost.getBruker()).isNull();
		assertThat(journalpost.getDokumenter())
				.hasSize(1)
				.flatExtracting(DokumentInfo::getDokumentvarianter)
				.extracting(Dokumentvariant::isSaksbehandlerHarTilgang)
				.containsExactly(true);
	}

	@Test
	void shouldQueryJournalpostWhenPensjonSak() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-psak-utgaaende-happy.json");
		stubPensjonBrukerForSak();
		stubPensjonSakSammendrag();
		stubPdl();

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		// Tema er uføretrygd pga det er arkivtema i pensjonsaken som gjelder
		assertThat(journalpost.getTema()).isEqualTo(UFO);
		assertThat(journalpost.getSak().getArkivsaksnummer()).isEqualTo("21998969");
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo("21998969");
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo("PP01");
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getSak().getTema()).isEqualTo(UFO);
		assertThat(journalpost.getSak().getDatoOpprettet()).isEqualTo(LocalDateTime.parse("2015-06-01T00:00"));
		assertThat(journalpost.getBruker().getId()).isEqualTo(AKTOER_ID);
		assertThat(journalpost.getBruker().getType()).isEqualTo(AKTOERID);
	}

	@Test
	void shouldQueryJournalpostAndNotFetchDokumenterWhenDokumenterIsNotInQuery() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-ingen-dokumenter.json", SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER);

		Journalpost journalpost = parseJournalpost(journalpostQuery("journalpost_ingen_dokumenter.query"));

		assertInngaaendeJournalpost(journalpost);
		assertThat(journalpost.getDokumenter()).isNull();
	}

	@Test
	void shouldQueryJournalpostAndIgnoreUkjentVariantformat() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-ukjent-variant.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());

		assertInngaaendeJournalpost(journalpost);
		List<Dokumentvariant> dokumentvarianter = journalpost.getDokumenter().get(0).getDokumentvarianter();
		assertThat(dokumentvarianter).hasSize(1);
		assertThat(dokumentvarianter.get(0).getVariantformat()).isEqualTo(ARKIV);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndNotNavBedrift() {
		abacPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndNavBedriftUnknownResponse() {
		abacPermit();
		stubNavHrOrganisasjon(ORG_NR, "hr-nav-organisasjon-error.txt");
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndNavBedriftEmptyResponse() {
		abacPermit();
		stubNavHrOrganisasjon(ORG_NR, "hr-nav-organisasjon-empty.json");
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndIsNavBedriftAndIsEgenAnsattBehandler() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldNotQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndIsNavBedriftAndIsNotEgenAnsattBehandler() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfNotEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, ORGNR_NAV_STAT);
		assertErrorWithMessage(graphQLResponse, "Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne.");
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndClientCredential() {
		abacPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQuery("journalpost.query", createHeadersClientCredential());
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndNotNavBedriftAndNavUserIdHeader() {
		abacPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQueryNavUserId();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndIsNavBedriftAndIsEgenAnsattBehandlerAndNavUserIdHeader() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQueryNavUserId();
		Journalpost journalpost = parseJournalpost(graphQLResponse);
		assertThat(journalpost).isNotNull();
		assertThat(journalpost.getBruker().getId()).isEqualTo("894705922");
		assertThat(journalpost.getBruker().getType()).isEqualTo(ORGNR);
	}

	@Test
	void shouldNotQueryJournalpostByJournalpostIdWhenOrgnummerOnSakAndIsNavBedriftAndIsNotEgenAnsattBehandlerAndNavUserIdHeader() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfNotEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-orgnr.json");

		GraphQLResponse graphQLResponse = journalpostQueryNavUserId();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, ORGNR_NAV_STAT);
		assertErrorWithMessage(graphQLResponse, "Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne.");
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep1g() {
		abacDenyPep1g();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-happy.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, UKJENT);
		assertErrorWithMessage(graphQLResponse, PEP1G_DENY_REASON);
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep2() {
		abacDenyPep2();
		stubBidrag();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-tema-far.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, TEMA);
		assertErrorWithMessage(graphQLResponse, PEP2_DENY_REASON);
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep2AndMidlertidigJournalpost() {
		abacDenyPep2MidlertidigJournalpost();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-midlertidig-tema-far.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, TEMA);
		assertErrorWithMessage(graphQLResponse, PEP2_DENY_REASON);
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseWhenDenyOnPep2d() {
		abacDenyPep2dSkipPep2();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang()).isFalse();
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep3() {
		abacDenyPep3SkipPep2();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-tema-bid.json");
		stubBidrag();

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, FORTROLIG_ADRESSE);
		assertErrorWithMessage(graphQLResponse, PEP3_DENY_REASON);
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep4() {
		abacDenyPep4SkipPep2Pep3();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-skjerming.json");

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCodeAndReason(graphQLResponse, FORBIDDEN, JOURNALSTATUS);
		assertErrorWithMessage(graphQLResponse, PEP4_DENY_REASON);
	}

	@Test
	void shouldReturnJournalpostWithOneFilteredDokumentInfoWhenDenyOnPep5() {
		abacDenyPep5SkipPep2Pep3Pep4();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-dokumentinfo-skjerming.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getDokumenter()).hasSize(1);
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseOnVariantWithDenyOnPep6d() {
		abacDenyPep6dSkipPep2Pep3Pep4Pep5();
		stubDokarkivJournalpost("journalpost-gsak-inngaaende-fildetaljer-skjerming.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getDokumenter())
				.hasSize(1)
				.flatExtracting(DokumentInfo::getDokumentvarianter)
				.hasSize(1)
				.extracting(Dokumentvariant::getVariantformat, Dokumentvariant::isSaksbehandlerHarTilgang)
				.contains(tuple(ARKIV, false));
	}

	@Test
	void shouldReturnErrorCodeNotFoundWhenJournalpostNotFound() {
		abacPermit();
		stubDokarkivJournalpost(HttpStatus.NOT_FOUND);

		assertErrorWithCode(journalpostQuery(), ErrorCode.NOT_FOUND);
	}

	@Test
	void shouldReturnErrorCodeServerErrorWhenJournalpostNotFound() {
		abacPermit();
		stubDokarkivJournalpost(INTERNAL_SERVER_ERROR);

		assertErrorWithCode(journalpostQuery(), SERVER_ERROR);
	}

	@Test
	void shouldRetryWhenNginxException() {
		abacPermit();
		stubDokarkivJournalpostRetry("journalpost-gsak-inngaaende-happy.json");

		Journalpost journalpost = parseJournalpost(journalpostQuery());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		verify(2, getRequestedFor(urlEqualTo("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)));
	}

	@Test
	void shouldReturnErrorCodeBadRequestWhenJournalpostIdNotValid() {
		assertErrorWithCode(journalpostQuery("journalpost_invalid_journalpostid.query"), BAD_REQUEST);
	}

	private void assertErrorWithCode(GraphQLResponse graphQLResponse, ErrorCode errorCode) {
		assertThat(graphQLResponse.getData().get("journalpost")).isNull();

		GraphQLResponse.Extensions errorExtensions = graphQLResponse.getErrors().get(0).getExtensions();
		assertThat(errorExtensions.getCode()).isEqualTo(errorCode.getText());
		assertThat(errorExtensions.getReasonCode()).isNull();
	}

	private void assertErrorWithCodeAndReason(GraphQLResponse graphQLResponse, ErrorCode errorCode, AbacDenyReasonCode reasonCode) {
		assertThat(graphQLResponse.getData().get("journalpost")).isNull();

		GraphQLResponse.Extensions errorExtensions = graphQLResponse.getErrors().get(0).getExtensions();
		assertThat(errorExtensions.getCode()).isEqualTo(errorCode.getText());
		assertThat(errorExtensions.getReasonCode()).isEqualTo(reasonCode.code);
		assertThat(errorExtensions.getReasonMessage()).isNotNull();
	}

	private void assertErrorWithMessage(GraphQLResponse graphQLResponse, String expectedErrorMessage) {
		assertThat(graphQLResponse.getData().get("journalpost")).isNull();
		assertThat(graphQLResponse.getErrors().get(0).getMessage()).contains(expectedErrorMessage);
	}

	@SneakyThrows
	private GraphQLResponse journalpostQuery() {
		return journalpostQuery("journalpost.query");
	}

	@SneakyThrows
	private GraphQLResponse journalpostQuery(String queryFile) {
		return journalpostQuery(queryFile, createHeaders());
	}

	@SneakyThrows
	private GraphQLResponse journalpostQueryNavUserId() {
		return journalpostQuery("journalpost.query", createHeadersNavUserId());
	}

	@SneakyThrows
	private GraphQLResponse journalpostQuery(String queryFile, HttpHeaders httpHeaders) {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("journalpost/" + queryFile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders, HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class).getBody();
	}

	private Journalpost parseJournalpost(GraphQLResponse graphQLResponse) {
		if (graphQLResponse.getData() == null) {
			return null;
		} else {
			Object journalpost = graphQLResponse.getData().get("journalpost");
			return OBJECT_MAPPER.convertValue(journalpost, Journalpost.class);
		}
	}

	private GraphQLResponse.Error parseJournalpostQueryError(GraphQLResponse graphQLResponse) {
		return graphQLResponse.getErrors().stream().findAny().orElse(null);
	}

	private static void stubDokarkivJournalpost(String fil) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpost/" + fil)));
	}

	private static void stubDokarkivJournalpost(String fil, Set<String> fields) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID + "?fields=" + String.join(",", fields))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpost/" + fil)));
	}

	private static void stubDokarkivJournalpost(HttpStatus httpStatus) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

	private static void stubDokarkivJournalpostEksternReferanseId(String fil) {
		stubFor(get("/dokarkiv/journalpost/eksternReferanseId/" + EKSTERNREFERANSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpost/" + fil)));
	}

	private static void stubDokarkivJournalpostRetry(String fil) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.inScenario("nginx_retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, TEXT_HTML_VALUE)
						.withBodyFile("nginx/nginx-notfound.html")))
				.setNewScenarioState("nginx_ok");
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.inScenario("nginx_retry")
				.whenScenarioStateIs("nginx_ok")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpost/" + fil)));
	}
}
