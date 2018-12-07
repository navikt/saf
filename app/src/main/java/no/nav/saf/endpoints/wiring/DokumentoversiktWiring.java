package no.nav.saf.endpoints.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerArguments;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerCoordinator;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktFagsakArguments;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktFagsakCoordinator;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
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
					try {
						DokumentoversiktBrukerArguments arguments = mapDokumentoversiktBrukerArguments(environment);
						SafRequestContext safRequestContext = environment.getContext();
						Dokumentoversikt dokumentoversikt = dokumentoversiktBrukerCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						logDokumentoversiktBrukerQueryDone(dokumentoversikt.getJournalposter().size(), arguments.getBrukerIdInput());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<Dokumentoversikt>(Dokumentoversikt.empty(), Collections.singletonList(e));
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktFagsak", environment -> {
					DokumentoversiktFagsakArguments arguments = mapDokumentoversiktFagsakArguments(environment);
					SafRequestContext safRequestContext = environment.getContext();
					try {
						Dokumentoversikt dokumentoversikt = dokumentoversiktFagsakCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktFagsak returnerer {} journalposter for  for fagsakId={} og fagsaksystem={}",
								dokumentoversikt.getJournalposter().size(), arguments.getFagsakId(), arguments.getFagsaksystem());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<Dokumentoversikt>(Dokumentoversikt.empty(), Collections.singletonList(e));
					}
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					Journalpost journalpost = environment.getSource();
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktBrukerCoordinator.findDokumenter(journalpost, safRequestContext);
				}))
				.build();
	}

	private DokumentoversiktBrukerArguments mapDokumentoversiktBrukerArguments(DataFetchingEnvironment environment) {
		Object brukerId = environment.getArgument("brukerId");
		BrukerIdInput brukerIdInput = mapper.convertValue(brukerId, BrukerIdInput.class);
		logDokumentoversiktBrukerQueryInit(brukerIdInput);
		LocalDate fraDato = environment.getArgument("fraDato");
		List<Tema> tema = environment.getArgument("tema");
		List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
		List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
		if(environment.getArgument("foerste") != null && environment.getArgument("siste") != null) {
			throw new IllegalArgumentException("Det er ikke tillatt å angi både `foerste` og `siste` for å paginere.");
		}
		if(environment.getArgument("foerste") != null && environment.getArgument("siste") != null) {
			throw new IllegalArgumentException("Du må angi en `foerste` eller en `siste` verdi for å paginere.");
		}
		Integer foerste = environment.getArgument("foerste");
		String etterPeker = environment.getArgument("etter");
		Integer siste = environment.getArgument("siste");
		String foerPeker = environment.getArgument("foer");
		return new DokumentoversiktBrukerArguments(brukerIdInput, fraDato, tema, journalposttyper, journalstatuser, foerste, etterPeker, siste, foerPeker);
	}

	private DokumentoversiktFagsakArguments mapDokumentoversiktFagsakArguments(DataFetchingEnvironment environment) {
		String fagsakId = environment.getArgument("fagsakId");
		String fagsaksystem = environment.getArgument("fagsaksystem");
		log.info("dokumentoversiktFagsak hentes for fagsakId={} og fagsaksystem={}", fagsakId, fagsaksystem);
		LocalDate fraDato = environment.getArgument("fraDato");
		List<Tema> tema = environment.getArgument("tema");
		List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
		List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
		if(environment.getArgument("foerste") != null && environment.getArgument("siste") != null) {
			throw new IllegalArgumentException("Det er ikke tillatt å angi både `foerste` og `siste` for å paginere.");
		}
		if(environment.getArgument("foerste") != null && environment.getArgument("siste") != null) {
			throw new IllegalArgumentException("Du må angi en `foerste` eller en `siste` verdi for å paginere.");
		}
		Integer foerste = environment.getArgument("foerste");
		String etterPeker = environment.getArgument("etter");
		Integer siste = environment.getArgument("siste");
		String foerPeker = environment.getArgument("foer");
		return new DokumentoversiktFagsakArguments(fagsakId, fagsaksystem, fraDato, tema, journalposttyper, journalstatuser, foerste, etterPeker, siste, foerPeker);
	}

	private void logDokumentoversiktBrukerQueryInit(BrukerIdInput brukerIdInput) {
		switch (brukerIdInput.getIdentType()) {
			case AKTOERID:
				log.info("dokumentoversiktBruker hentes for bruker med aktoerId={}", brukerIdInput.getIdent());
				break;
			case FNR:
				log.info("dokumentoversiktBruker hentes for bruker med fødselsnummer={}", "*****"); // vi kan ikke logge fnr
				break;
			default:
				// noop
				break;
		}
	}

	private void logDokumentoversiktBrukerQueryDone(int numJournalposter, BrukerIdInput brukerIdInput) {
		switch (brukerIdInput.getIdentType()) {
			case AKTOERID:
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med aktoerId={}", numJournalposter, brukerIdInput
						.getIdent());
				break;
			case FNR:
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med fødselsnummer={}", numJournalposter, "*****"); // vi kan ikke logge fnr
				break;
			default:
				// noop
				break;
		}
	}
}
