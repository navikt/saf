package no.nav.saf.endpoints.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.DataFetcherResult;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerArguments;
import no.nav.saf.tjeneste.dokumentoversiktbruker.DokumentoversiktBrukerCoordinator;
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
	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	public DokumentoversiktWiring(DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator) {
		this.dokumentoversiktBrukerCoordinator = dokumentoversiktBrukerCoordinator;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DateScalar.DATE)
				.scalar(DateTimeScalar.DATE_TIME)
				.type("Tema", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Tema.class)))
				.type("Journalposttype", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalposttype.class)))
				.type("Journalstatus", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalstatus.class)))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktBruker", environment -> {
					Object brukerId = environment.getArgument("brukerId");
					BrukerIdInput brukerIdInput = mapper.convertValue(brukerId, BrukerIdInput.class);
					logDokumentoversiktBrukerQueryInit(brukerIdInput);
					LocalDate fraDato = environment.getArgument("fraDato");
					List<Tema> tema = environment.getArgument("tema");
					List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
					List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
					SafRequestContext safRequestContext = environment.getContext();
					try {
						List<Journalpost> journalposter = dokumentoversiktBrukerCoordinator.findJournalposter(
								new DokumentoversiktBrukerArguments(brukerIdInput, fraDato, tema, journalposttyper, journalstatuser),
								safRequestContext);
						logDokumentoversiktBrukerQueryDone(journalposter.size(), brukerIdInput);
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
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med aktoerId={}", numJournalposter, brukerIdInput.getIdent());
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
