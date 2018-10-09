package no.nav.saf;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Bruker;
import no.nav.saf.domain.DokumentInfo;
import no.nav.saf.domain.DokumentStatus;
import no.nav.saf.domain.Fagsystem;
import no.nav.saf.domain.Journalpost;
import no.nav.saf.domain.JournalpostStatus;
import no.nav.saf.domain.JournalpostType;
import no.nav.saf.domain.Sak;
import no.nav.saf.domain.Sakssystem;
import no.nav.saf.domain.Tema;
import no.nav.saf.domain.Temakode;
import no.nav.saf.domain.TilknyttetJournalpostSom;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SakQuery {
	@GraphQLQuery(name = "saker")
	public List<Sak> temaer(@GraphQLArgument(name = "datoFra") LocalDate datoFra, @GraphQLContext Tema tema) {
		Faker faker = new Faker();
		return Stream.of(
				tema.getTema() == Temakode.BID ? bidrag(faker) : null,
				tema.getTema() == Temakode.FOR ? foreldrepenger(faker) : null
		).filter(Objects::nonNull).filter(p -> p.getDatoOpprettet().toLocalDate().isAfter(Optional.ofNullable(datoFra).orElse(LocalDate.MIN))).collect(Collectors.toList());
	}

	private Sak foreldrepenger(Faker faker) {
		return Sak.builder()
				.saksreferanse(faker.number().digits(7))
				.sakssystem(Sakssystem.GSAK)
				.fagsaksreferanse(faker.number().digits(5))
				.fagsystem(Fagsystem.VEDTAKSLOSNING_FORELDREPENGER)
				.tema(Temakode.FOR)
				.datoOpprettet(LocalDateTime.now())
				.journalposter(Arrays.asList(
						Journalpost.builder()
								.journalpostId(faker.number().digits(9))
								.beskrivelse("Søknad om engangsstønad")
								.avsender(faker.number().digits(11))
								.tema(Temakode.FOR)
								.type(JournalpostType.INNGAENDE)
								.status(JournalpostStatus.JOURNALFOERT)
								.dokumentInfo(Arrays.asList(
										DokumentInfo.builder()
												.dokumentId(faker.number().digits(9))
												.tittel("Søknad om engangsstønad")
												.dokumenttypeId("I0000499")
												.dokumentStatus(DokumentStatus.INNSENDT)
												.tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
												.build(),
										DokumentInfo.builder()
												.dokumentId(faker.number().digits(9))
												.tittel("Terminbekreftelse")
												.dokumenttypeId("I0000500")
												.dokumentStatus(DokumentStatus.INNSENDT)
												.tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
												.build()

								))
								.build()
				))
				.build();
	}

	private Sak bidrag(Faker faker) {
		return Sak.builder()
				.saksreferanse(faker.number().digits(7))
				.sakssystem(Sakssystem.GSAK)
				.fagsaksreferanse(faker.number().digits(5))
				.fagsystem(Fagsystem.BIDRAG)
				.tema(Temakode.BID)
				.datoOpprettet(LocalDateTime.now().minusMonths(2))
				.journalposter(Arrays.asList(
						Journalpost.builder()
								.journalpostId(faker.number().digits(9))
								.beskrivelse("Søknad om forsørgerbidrag")
								.avsender(faker.number().digits(11))
								.tema(Temakode.BID)
								.type(JournalpostType.INNGAENDE)
								.status(JournalpostStatus.JOURNALFOERT)
								.dokumentInfo(Arrays.asList(
										DokumentInfo.builder()
												.dokumentId(faker.number().digits(9))
												.tittel("Søknad om forsørgerbidrag")
												.dokumenttypeId("I0000342")
												.dokumentStatus(DokumentStatus.INNSENDT)
												.tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
												.build(),
										DokumentInfo.builder()
												.dokumentId(faker.number().digits(9))
												.tittel("Dokumentasjon på forsørgerbidrag")
												.dokumenttypeId("I0000343")
												.dokumentStatus(DokumentStatus.INNSENDT)
												.tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
												.build()

								))
								.build()
						, Journalpost.builder()
								.journalpostId(faker.number().digits(9))
								.beskrivelse("Vedtak om forsørgerbidrag")
								.avsender(faker.number().digits(11))
								.tema(Temakode.BID)
								.type(JournalpostType.UTGAAENDE)
								.status(JournalpostStatus.JOURNALFOERT)
								.dokumentInfo(Arrays.asList(
										DokumentInfo.builder()
												.dokumentId(faker.number().digits(9))
												.tittel("Vedtak om forsørgerbidrag")
												.dokumenttypeId("0000200")
												.dokumentStatus(DokumentStatus.FERDIGSTILT)
												.tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
												.build()

								))
								.build()
				))
				.build();
	}
}
