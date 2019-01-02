package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
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

	private final JournalpostDtoMapper mapper = new JournalpostDtoMapper();

	@Test
	void shouldMapJournalpostDtoWithUtgaaendeJournalpost() {

		String dokumentInfoId = "1234";
		String tittel = "viktigtittel";
		VariantFormatCode variantFormatCode = VariantFormatCode.ARKIV;
		String brevkode = "brevkodeX";

		List<DokumentInfoDto> dokumenter =
				Collections.singletonList(
						DokumentInfoDto.builder()
								.dokumentInfoId(dokumentInfoId)
								.tittel(tittel)
								.variantFormat(variantFormatCode)
								.brevkode(brevkode)
								.logiske(Collections.singletonList(new LogiskVedleggDto()))
								.build());


		long journalpostId = 417457822L;
		String innhold = "MASKERT_FELT";
		FagomradeCode fagområde = FagomradeCode.STO;
		JournalStatusCode journalStatusCode = JournalStatusCode.E;
		String journalfoertAv = "Automatisk jobb";
		UtsendingsKanalCode utsendingsKanalCode = UtsendingsKanalCode.S;
		MottaksKanalCode mottaksKanalCode = MottaksKanalCode.ALTINN;
		JournalpostTypeCode journalposttype = JournalpostTypeCode.U;
		Date datoOpprettet = new Date(1000L);
		Date avsReturDato = new Date(2000L);
		Date sendtPrintDato = new Date(3000L);
		Date ekspedertDato = new Date(4000L);
		Date dokumentDato = new Date(5000L);
		Date journalDato = new Date(6000L);
		Date mottattDato = new Date(7000L);


		String saksId = "12345";
		FagsystemCode fagsystemCode = FagsystemCode.FS22;

		JournalpostDto journalpostDto = JournalpostDto.builder()
				.journalpostId(journalpostId)
				.nextJournalpostId(405252858L)
				.innhold(innhold)
				.fagomrade(fagområde)
				.journalstatus(journalStatusCode)
				.journalfortAvNavn(journalfoertAv)
				.utsendingskanal(utsendingsKanalCode)
				.mottakskanal(mottaksKanalCode)
				.journalposttype(journalposttype)
				.saksrelasjon(new SaksrelasjonDto(saksId, false, fagsystemCode))
				.datoOpprettet(datoOpprettet)
				.dokumentDato(dokumentDato)
				.avsReturDato(avsReturDato)
				.sendtPrintDato(sendtPrintDato)
				.ekspedertDato(ekspedertDato)
				.dokumenter(dokumenter)
				.build();

		String fnr = "***gammelt_fnr***";
		String aktoerId = "***gammelt_fnr***31";

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(TILGANG_BRUKER,
				TilgangBruker.builder()
						.foedselsnr(fnr)
						.aktoerId(aktoerId)
						.build()
		);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Long.toString(journalpostId), journalpost.getJournalpostId());
		assertEquals(innhold, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafJournalstatus(fagområde), journalpost.getTema());
		assertEquals(journalStatusCode.toSafJournalStatus(), journalpost.getJournalstatus());
		assertEquals(journalfoertAv, journalpost.getJournalfortAvNavn());
		assertEquals(utsendingsKanalCode.getSafKanal().getKanalnavn(), journalpost.getKanalnavn());
		assertEquals(journalposttype.toString(), journalpost.getJournalposttype().toString());
		assertEquals(LocalDateTime.from(datoOpprettet.toInstant().atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));

		assertEquals(1, journalpost.getDokumenter().size());
		assertEquals(dokumentInfoId, journalpost.getDokumenter().get(0).getDokumentId());
		assertEquals(variantFormatCode.toString(), journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getVariantformat().toString());
		assertEquals(brevkode, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(brevkode, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(aktoerId, journalpost.getBruker().getId());

		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(dokumentDato, Datotype.DATO_DOKUMENT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(avsReturDato, Datotype.DATO_AVS_RETUR)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(sendtPrintDato, Datotype.DATO_SENDT_PRINT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(ekspedertDato, Datotype.DATO_EKSPEDERT)));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(mottattDato, Datotype.DATO_MOTTATT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(journalDato, Datotype.DATO_JOURNALFOERT))));

	}

	@Test
	void shouldMapJournalpostDtoWithInngaaendeJournalpost() {

		String dokumentInfoId = "1234";
		String tittel = "viktigtittel";
		VariantFormatCode variantFormatCode = VariantFormatCode.ARKIV;
		String brevkode = "brevkodeX";

		List<DokumentInfoDto> dokumenter =
				Collections.singletonList(
						DokumentInfoDto.builder()
								.dokumentInfoId(dokumentInfoId)
								.tittel(tittel)
								.variantFormat(variantFormatCode)
								.brevkode(brevkode)
								.logiske(Collections.singletonList(new LogiskVedleggDto()))
								.build());

		long journalpostId = 417457822L;
		String innhold = "MASKERT_FELT";
		FagomradeCode fagområde = FagomradeCode.STO;
		JournalStatusCode journalStatusCode = JournalStatusCode.E;
		String journalfoertAv = "Automatisk jobb";
		UtsendingsKanalCode utsendingsKanalCode = UtsendingsKanalCode.S;
		MottaksKanalCode mottaksKanalCode = MottaksKanalCode.ALTINN;
		JournalpostTypeCode journalposttype = JournalpostTypeCode.I;
		Date datoOpprettet = new Date(1000L);
		Date avsReturDato = new Date(2000L);
		Date sendtPrintDato = new Date(3000L);
		Date ekspedertDato = new Date(4000L);
		Date dokumentDato = new Date(5000L);
		Date journalDato = new Date(6000L);
		Date mottattDato = new Date(7000L);


		String saksId = "12345";
		FagsystemCode fagsystemCode = FagsystemCode.FS22;

		JournalpostDto journalpostDto = JournalpostDto.builder()
				.journalpostId(journalpostId)
				.nextJournalpostId(405252858L)
				.innhold(innhold)
				.fagomrade(fagområde)
				.journalstatus(journalStatusCode)
				.journalfortAvNavn(journalfoertAv)
				.utsendingskanal(utsendingsKanalCode)
				.mottakskanal(mottaksKanalCode)
				.journalposttype(journalposttype)
				.saksrelasjon(new SaksrelasjonDto(saksId, true, fagsystemCode))
				.datoOpprettet(datoOpprettet)
				.mottattDato(mottattDato)
				.journalDato(journalDato)
				.dokumenter(dokumenter)
				.build();

		String aktoerId = "***gammelt_fnr***31";

		RequestCache requestCache = new RequestCache();
		requestCache.putObject(saksId + Arkivsakssystem.GSAK.name(),
				Arkivsak.builder()
						.aktoerId(aktoerId)
						.build()
		);

		Journalpost journalpost = mapper.mapJournalpostDto(journalpostDto, requestCache);

		assertEquals(Long.toString(journalpostId), journalpost.getJournalpostId());
		assertEquals(innhold, journalpost.getTittel());
		assertEquals(FagomradeCode.toSafJournalstatus(fagområde), journalpost.getTema());
		assertEquals(Journalstatus.FEILREGISTRERT, journalpost.getJournalstatus());
		assertEquals(journalfoertAv, journalpost.getJournalfortAvNavn());
		assertEquals(mottaksKanalCode.getSafKanal().getKanalnavn(), journalpost.getKanalnavn());
		assertEquals(journalposttype.toString(), journalpost.getJournalposttype().toString());
		assertEquals(LocalDateTime.from(datoOpprettet.toInstant().atZone(ZoneId.systemDefault())), LocalDateTime.from(journalpost.getDatoOpprettet()));

		assertEquals(1, journalpost.getDokumenter().size());
		assertEquals(dokumentInfoId, journalpost.getDokumenter().get(0).getDokumentId());
		assertEquals(variantFormatCode.toString(), journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getVariantformat().toString());
		assertEquals(brevkode, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(brevkode, journalpost.getDokumenter().get(0).getNavSkjemaId());
		assertEquals(aktoerId, journalpost.getBruker().getId());

		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(dokumentDato, Datotype.DATO_DOKUMENT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(avsReturDato, Datotype.DATO_AVS_RETUR))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(sendtPrintDato, Datotype.DATO_SENDT_PRINT))));
		assertThat(journalpost.getRelevanteDatoer(), not(hasItem(new RelevantDato(ekspedertDato, Datotype.DATO_EKSPEDERT))));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(mottattDato, Datotype.DATO_MOTTATT)));
		assertThat(journalpost.getRelevanteDatoer(), hasItem(new RelevantDato(journalDato, Datotype.DATO_JOURNALFOERT)));
	}

}