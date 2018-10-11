package no.nav.saf.sakstilknyttedejournalposter;

import static no.nav.saf.domain.kode.Temakode.BID;
import static no.nav.saf.domain.kode.Temakode.FOR;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Tema;
import no.nav.saf.domain.kode.AvsenderType;
import no.nav.saf.domain.DokumentInfo;
import no.nav.saf.domain.kode.DokumentStatus;
import no.nav.saf.domain.kode.Dokumentkategori;
import no.nav.saf.domain.kode.JournalTilstand;
import no.nav.saf.domain.Journalpost;
import no.nav.saf.domain.kode.JournalpostStatus;
import no.nav.saf.domain.kode.JournalpostType;
import no.nav.saf.domain.kode.Mottakskanal;
import no.nav.saf.domain.Sak;
import no.nav.saf.domain.kode.Temakode;
import no.nav.saf.domain.kode.TilknyttetJournalpostSom;
import no.nav.saf.domain.kode.Utsendingskanal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostQuery {
	@GraphQLQuery(name = "journalposter")
	public List<Journalpost> journalposter(@GraphQLContext Sak sak) {
		Faker faker = new Faker();
		switch (sak.getTemakode()) {
			case BID:
				return bidrag(faker);
			case FOR:
				return foreldrepenger(faker);
			default:
				return new ArrayList<>();
		}
	}

	private List<Journalpost> foreldrepenger(Faker faker) {
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

	private List<Journalpost> bidrag(Faker faker) {
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
