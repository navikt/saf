package no.nav.saf;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Fagsystem;
import no.nav.saf.domain.Sak;
import no.nav.saf.domain.SakType;
import no.nav.saf.domain.Sakssystem;
import no.nav.saf.domain.Tema;
import no.nav.saf.domain.Temakode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
				.sakType(SakType.FAGSYSTEM_SAK)
				.saksreferanse(faker.number().digits(7))
				.sakssystem(Sakssystem.GSAK)
				.fagsaksreferanse(faker.number().digits(5))
				.fagsystem(Fagsystem.VEDTAKSLOSNING_FORELDREPENGER)
				.tema(Temakode.FOR)
				.datoOpprettet(LocalDateTime.now())
				.build();
	}

	private Sak bidrag(Faker faker) {
		return Sak.builder()
				.sakType(SakType.FAGSYSTEM_SAK)
				.saksreferanse(faker.number().digits(7))
				.sakssystem(Sakssystem.GSAK)
				.fagsaksreferanse(faker.number().digits(5))
				.fagsystem(Fagsystem.BISYS)
				.tema(Temakode.BID)
				.datoOpprettet(LocalDateTime.now().minusMonths(2))
				.build();
	}
}
