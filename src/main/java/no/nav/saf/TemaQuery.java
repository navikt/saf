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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TemaQuery {
	@GraphQLQuery(name = "temaer")
	public Set<Tema> temaer(@GraphQLArgument(name = "tema", defaultValue = "[]") List<Temakode> tema, @GraphQLContext Bruker bruker) {
		Faker faker = new Faker();
		return Stream.of(
				tema.contains(Temakode.BID) || tema.isEmpty() ? bidrag(faker) : null,
				tema.contains(Temakode.FOR) || tema.isEmpty() ? foreldrepenger(faker) : null
		).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private Tema foreldrepenger(Faker faker) {
		return Tema.builder()
				.tema(Temakode.FOR)
				.build();
	}

	private Tema bidrag(Faker faker) {
		return Tema.builder()
				.tema(Temakode.BID)
				.saker(Arrays.asList(
)
				).build();
	}
}
