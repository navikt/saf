package no.nav.saf.endpoints.sakstilknyttedejournalposter;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import no.nav.saf.coordinator.SafDomainCoordinator;
import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tema;
import no.nav.saf.domain.visningsmodell.kode.JournalpostStatus;
import no.nav.saf.domain.visningsmodell.kode.JournalpostType;
import no.nav.saf.domain.visningsmodell.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SakstilknyttedeJournalposter {

	private final SafDomainCoordinator coordinator;

	@Inject
	public SakstilknyttedeJournalposter(SafDomainCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	@GraphQLQuery(name = "sakstilknyttedeJournalposterBy")
	public Bruker sakstilknyttedeJournalposterBy(@GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId,
												 @GraphQLRootContext Map<String, Object> rootContext) {
		rootContext.put("aktoerId", aktoerId);
		return coordinator.findBrukerByAktoerId(aktoerId);
	}

	@GraphQLQuery(name = "temaer")
	public Set<Tema> temaer(@GraphQLContext Bruker bruker,
							@GraphQLArgument(name = "tema", defaultValue = "[]") List<Temakode> tema,
							@GraphQLRootContext("aktoerId") String aktoerId) {
		return coordinator.findTemaKnyttetTilAktoerIdAndFilterByTemakoder(aktoerId, tema);
	}

	@GraphQLQuery(name = "saker")
	public List<Sak> saker(@GraphQLContext Tema tema,
						   @GraphQLRootContext("aktoerId") String aktoerId,
						   @GraphQLRootContext Map<String, Object> rootContext) {
		rootContext.put("tema", tema.getTema());
		return coordinator.findSakerByAktoerIdAndTema(aktoerId, tema.getTema());
	}

	@GraphQLQuery(name = "journalposter")
	public List<Journalpost> journalposter(@GraphQLContext Sak sak,
										   @GraphQLArgument(name = "journalposttype", defaultValue = "[]") List<JournalpostType> journalpostType,
										   @GraphQLArgument(name = "journalstatus", defaultValue = "[]") List<JournalpostStatus> journalstatus,
										   @GraphQLRootContext Map<String, Object> rootContext) {
		rootContext.put("arkivsaksnummer", sak.getArkivsaksnummer());
		return coordinator.findJournalposterByArkivsak(sak.getArkivsaksnummer()).stream()
				.filter(journalpost -> journalpostType.isEmpty() || journalpostType.contains(journalpost.getJournalposttype()))
				.filter(journalpost -> journalstatus.isEmpty() || journalstatus.contains(journalpost.getJournalstatus()))
				.collect(Collectors.toList());
	}

	@GraphQLQuery(name = "dokumenter")
	public List<DokumentInfo> dokumenter(@GraphQLContext Journalpost journalpost,
										 @GraphQLRootContext("arkivsaksnummer") String arkivsaksnummer) {
		return coordinator.findDokumentInfoByJournalpostIdAndArkivsak(journalpost.getJournalpostID(), arkivsaksnummer);
	}
}
