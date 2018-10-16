package no.nav.saf.context.mock;

import com.github.javafaker.Faker;
import no.nav.saf.context.saf.domain.DokumentInfo;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.context.saf.domain.kode.DokumentStatus;
import no.nav.saf.context.saf.domain.kode.Dokumentkategori;
import no.nav.saf.context.saf.domain.kode.Fagsystem;
import no.nav.saf.context.saf.domain.kode.JournalpostStatus;
import no.nav.saf.context.saf.domain.kode.JournalpostType;
import no.nav.saf.context.saf.domain.kode.Mottakskanal;
import no.nav.saf.context.saf.domain.kode.Temakode;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class MockData {
	public static List<Sak> foreldrepengesaker(Faker faker) {
		return Collections.singletonList(Sak.builder()
				.arkivsaksnummer(faker.number().digits(7))
				.arkivsakssystem(Arkivsakssystem.GSAK)
				.fagsaksnummer(faker.number().digits(5))
				.fagsystem(Fagsystem.VEDTAKSLOSNING_FORELDREPENGER.name())
				.datoOpprettet(OffsetDateTime.now())
				.temakode(Temakode.FOR)
				.build());
	}

	public static List<Sak> bidragsaker(Faker faker) {
		return Collections.singletonList(Sak.builder()
				.arkivsaksnummer(faker.number().digits(7))
				.arkivsakssystem(Arkivsakssystem.GSAK)
				.fagsaksnummer(faker.number().digits(5))
				.fagsystem(Fagsystem.BISYS.name())
				.temakode(Temakode.BID)
				.datoOpprettet(OffsetDateTime.now().minusMonths(2))
				.build());
	}

	public static List<Journalpost> foreldrepengerjournalposter(Faker faker) {
		return Arrays.asList(
				Journalpost.builder()
						.journalpostID(faker.number().digits(9))
						.journalposttype(JournalpostType.INNGAENDE)
						.journalstatus(JournalpostStatus.JOURNALFOERT)
						.journalposttittel("Søknad om engangsstønad")
						.mottakskanal(Mottakskanal.NAV_NO)
						.avsenderID(faker.number().digits(11))
						.avsenderNavn(faker.name().fullName())
						.opprettet(LocalDateTime.now())
						.dokumentInfo(Arrays.asList(
								DokumentInfo.builder()
										.dokumentID(faker.number().digits(9))
										.tittel("Søknad om engangsstønad")
										.dokumenttypeID("I0000499")
										.navSkjemaID("NavSkjema0")
										.dokumentStatus(DokumentStatus.INNSENDT)
										.dokumentkategori(Dokumentkategori.SOKNAD)
										.build(),
								DokumentInfo.builder()
										.dokumentID(faker.number().digits(9))
										.tittel("Dokumentasjon på inntekt")
										.dokumenttypeID("I0000498")
										.navSkjemaID("NavSkjema1")
										.dokumentStatus(DokumentStatus.INNSENDT)
										.dokumentkategori(Dokumentkategori.SOKNAD)
										.build()

						))
						.build()
		);
	}

	public static List<Journalpost> bidragjournalposter(Faker faker) {
		return Arrays.asList(
				Journalpost.builder()
						.journalpostID(faker.number().digits(9))
						.journalposttittel("Søknad om forsørgerbidrag")
						.avsenderID(faker.number().digits(11))
						.avsenderNavn(faker.name().fullName())
						.journalposttype(JournalpostType.INNGAENDE)
						.journalstatus(JournalpostStatus.JOURNALFOERT)
						.opprettet(LocalDateTime.now())
						.mottakskanal(Mottakskanal.NAV_NO)
						.dokumentInfo(Arrays.asList(
								DokumentInfo.builder()
										.dokumentID(faker.number().digits(9))
										.tittel("Søknad om forsørgerbidrag")
										.dokumenttypeID("I0000342")
										.dokumentStatus(DokumentStatus.INNSENDT)
										.dokumentkategori(Dokumentkategori.SOKNAD)
										.build(),
								DokumentInfo.builder()
										.dokumenttypeID(faker.number().digits(9))
										.tittel("Dokumentasjon på forsørgerbidrag")
										.dokumenttypeID("I0000343")
										.dokumentStatus(DokumentStatus.INNSENDT)
										.dokumentkategori(Dokumentkategori.SOKNAD)
										.build()

						))
						.build()
		);
	}
}
