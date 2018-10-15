package no.nav.saf.sakstilknyttedejournalposter;

import com.github.javafaker.Faker;
import graphql.schema.DataFetchingEnvironment;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import io.leangen.graphql.execution.ResolutionEnvironment;
import no.nav.saf.context.saf.domain.Bruker;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import no.nav.saf.repository.SakstilknyttedeJournalposterRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	public Bruker sakstilknyttedeJournalposterBy(@GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId,
												 @GraphQLRootContext Map<String, Object> rootContext) {
		rootContext.put("aktoerId", aktoerId);
		return repository.findBrukerByAktoerId(aktoerId);
	}

	@GraphQLQuery(name = "temaer")
	public Set<Tema> temaer(@GraphQLContext Bruker bruker,
							@GraphQLArgument(name = "tema", defaultValue = "[]") List<Temakode> tema,
							@GraphQLRootContext("aktoerId") String aktoerId) {
		return repository.findTemaKnyttetTilAktoerIdAndFilterByTemakoder(aktoerId, tema);
	}

	@GraphQLQuery(name = "saker")
	public List<Sak> saker(@GraphQLContext Tema tema,
						   @GraphQLRootContext("aktoerId") String aktoerId,
						   @GraphQLRootContext Map<String, Object> rootContext) {
		rootContext.put("tema", tema.getTema());
		return repository.findSakerByAktoerIdAndTema(aktoerId, tema.getTema());
	}

	@GraphQLQuery(name = "journalposter")
	public List<Journalpost> journalposter(@GraphQLContext Sak sak,
										   @GraphQLRootContext("tema") Temakode tema) {
		return repository.findJournalposterByFagsakAndTema(sak.getFagsaksnummer(), tema);
	}
}
