package no.nav.saf.endpoints.sakstilknyttedejournalposter;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.SakstilknyttedeJournalposterDomainCoordinator;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Bruker;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Sak;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Tema;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.JournalpostStatus;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SakstilknyttedeJournalposterFor {

	private final SakstilknyttedeJournalposterDomainCoordinator coordinator;

	@Inject
	public SakstilknyttedeJournalposterFor(SakstilknyttedeJournalposterDomainCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	@GraphQLQuery(name = "sakstilknyttedeJournalposterFor")
	public Bruker sakstilknyttedeJournalposterFor(@GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId,
												 @GraphQLRootContext SafRequestContext safRequestContext) {
		safRequestContext.setAktoerId(aktoerId);
		return coordinator.findBrukerByAktoerId(aktoerId, safRequestContext);
	}

	@GraphQLQuery(name = "temaer")
	public Set<Tema> temaer(@GraphQLContext Bruker bruker,
							@GraphQLArgument(name = "tema", defaultValue = "[]") List<Temakode> tema,
							@GraphQLRootContext("aktoerId") String aktoerId) {
		return new HashSet<>();
	}

	@GraphQLQuery(name = "saker")
	public List<Sak> saker(@GraphQLContext Tema tema,
						   @GraphQLRootContext("aktoerId") String aktoerId,
						   @GraphQLRootContext SafRequestContext safRequestContext) {
		return new ArrayList<>();
	}

	@GraphQLQuery(name = "journalposter")
	public List<Journalpost> journalposter(@GraphQLContext Sak sak,
										   @GraphQLArgument(name = "journalposttype", defaultValue = "[]") List<JournalpostType> journalpostType,
										   @GraphQLArgument(name = "journalstatus", defaultValue = "[]") List<JournalpostStatus> journalstatus,
										   @GraphQLRootContext SafRequestContext safRequestContext) {
		return new ArrayList<>();
	}

	@GraphQLQuery(name = "dokumenter")
	public List<DokumentInfo> dokumenter(@GraphQLContext Journalpost journalpost,
										 @GraphQLRootContext("arkivsaksnummer") String arkivsaksnummer) {
		return new ArrayList<>();
	}
}
