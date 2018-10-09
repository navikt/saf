package no.nav.saf;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Bruker;
import no.nav.saf.domain.Tema;
import no.nav.saf.domain.Temakode;
import org.springframework.stereotype.Component;

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
		return Stream.of(
				tema.contains(Temakode.BID) || tema.isEmpty() ? bidrag() : null,
				tema.contains(Temakode.FOR) || tema.isEmpty() ? foreldrepenger() : null
		).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private Tema foreldrepenger() {
		return Tema.builder()
				.tema(Temakode.FOR)
				.build();
	}

	private Tema bidrag() {
		return Tema.builder()
				.tema(Temakode.BID)
				.build();
	}
}
