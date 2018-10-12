package no.nav.saf.sakstilknyttedejournalposter;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.context.saf.domain.Bruker;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import no.nav.saf.repository.SakstilknyttedeJournalposterRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class SakstilknyttedeJournalposter {

	private final SakstilknyttedeJournalposterRepository repository;

	@Inject
	public SakstilknyttedeJournalposter(SakstilknyttedeJournalposterRepository repository) {
		this.repository = repository;
	}

	@GraphQLQuery(name = "sakstilknyttedeJournalposterBy")
	public Bruker sakstilknyttedeJournalposterBy(final @GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId) {
		return repository.findBrukerByAktoerId(aktoerId);
	}

	@GraphQLQuery(name = "temaer")
	public Set<Tema> temaer(@GraphQLContext Bruker bruker, @GraphQLArgument(name = "tema", defaultValue = "[]") List<Temakode> tema) {
		return repository.findTemaKnyttetTilAktoerIdAndFilterByTemakoder(bruker.getAktoerId(), tema);
	}
}
