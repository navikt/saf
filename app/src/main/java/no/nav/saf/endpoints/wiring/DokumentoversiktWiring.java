package no.nav.saf.endpoints.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.DataFetcherResult;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerArguments;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerCoordinator;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktFagsakArguments;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktFagsakCoordinator;
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class DokumentoversiktWiring {
	private final DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator;
	private final DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator;
	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	public DokumentoversiktWiring(DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator,
								  DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator) {
		this.dokumentoversiktBrukerCoordinator = dokumentoversiktBrukerCoordinator;
		this.dokumentoversiktFagsakCoordinator = dokumentoversiktFagsakCoordinator;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DateScalar.DATE)
				.scalar(DateTimeScalar.DATE_TIME)
				.type("Tema", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Tema.class)))
				.type("Journalposttype", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalposttype.class)))
				.type("Journalstatus", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalstatus.class)))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktBruker", environment -> {
					Object brukeridentifikatorObject = environment.getArgument("brukeridentifikator");
					Brukeridentifikator brukeridentifikator = mapper.convertValue(brukeridentifikatorObject, Brukeridentifikator.class);
					logDokumentoversiktBrukerQueryInit(brukeridentifikator);
					LocalDate fraDato = environment.getArgument("fraDato");
					List<Tema> tema = environment.getArgument("tema");
					List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
					List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
					SafRequestContext safRequestContext = environment.getContext();
					try {
						List<Journalpost> journalposter = dokumentoversiktBrukerCoordinator.findJournalposter(
								new DokumentoversiktBrukerArguments(brukeridentifikator, fraDato, tema, journalposttyper, journalstatuser),
								safRequestContext);
						logDokumentoversiktBrukerQueryDone(journalposter.size(), brukeridentifikator);
						return journalposter;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<List<Journalpost>>(new ArrayList<>(), Collections.singletonList(e));
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktFagsak", environment -> {
					String fagsakId = environment.getArgument("fagsakId");
					String fagsaksystem = environment.getArgument("fagsaksystem");
					log.info("dokumentoversiktFagsak hentes for fagsakId={} og fagsaksystem={}", fagsakId, fagsaksystem);
					LocalDate fraDato = environment.getArgument("fraDato");
					List<Tema> tema = environment.getArgument("tema");
					List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
					List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
					SafRequestContext safRequestContext = environment.getContext();
					try {
						List<Journalpost> journalposter = dokumentoversiktFagsakCoordinator.findJournalposter(
								new DokumentoversiktFagsakArguments(fagsakId, fagsaksystem, fraDato, tema, journalposttyper, journalstatuser),
								safRequestContext);
						log.info("dokumentoversiktBruker returnerer {} journalposter for  for fagsakId={} og fagsaksystem={}",
								journalposter.size(), fagsakId, fagsaksystem);
						return journalposter;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<List<Journalpost>>(new ArrayList<>(), Collections.singletonList(e));
					}
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					Journalpost journalpost = environment.getSource();
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktBrukerCoordinator.findDokumenter(journalpost, safRequestContext);
				}))
				.build();
	}

	private void logDokumentoversiktBrukerQueryInit(Brukeridentifikator brukeridentifikator) {
		switch (brukeridentifikator.getIdentType()) {
			case AKTOERID:
				log.info("dokumentoversiktBruker hentes for bruker med aktoerId={}", brukeridentifikator.getIdent());
				break;
			case FOEDSELSNUMMER:
				log.info("dokumentoversiktBruker hentes for bruker med fødselsnummer={}", "*****"); // vi kan ikke logge fnr
				break;
			default:
				// noop
				break;
		}
	}

	private void logDokumentoversiktBrukerQueryDone(int numJournalposter, Brukeridentifikator brukeridentifikator) {
		switch (brukeridentifikator.getIdentType()) {
			case AKTOERID:
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med aktoerId={}", numJournalposter, brukeridentifikator
						.getIdent());
				break;
			case FOEDSELSNUMMER:
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med fødselsnummer={}", numJournalposter, "*****"); // vi kan ikke logge fnr
				break;
			default:
				// noop
				break;
		}
	}
}
