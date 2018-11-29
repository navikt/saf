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
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
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
				.type("Journalposttype", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalposttype.class)))
				.type("Journalstatus", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalstatus.class)))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktBruker", environment -> {
					Object brukeridentifikatorObject = environment.getArgument("brukeridentifikator");
					Brukeridentifikator brukeridentifikator = mapper.convertValue(brukeridentifikatorObject, Brukeridentifikator.class);
					logDokumentoversiktBrukerQueryInit(brukeridentifikator);
					LocalDate fraDato = environment.getArgument("fraDato");
					List<Journalposttype> journalposttyper = environment.getArgument("journalposttyper");
					List<Journalstatus> journalstatuser = environment.getArgument("journalstatuser");
					SafRequestContext safRequestContext = environment.getContext();
					try {
						List<Journalpost> journalposter = dokumentoversiktBrukerCoordinator.findJournalposter(new DokumentoversiktBrukerArguments(brukeridentifikator, fraDato, journalposttyper, journalstatuser),
								safRequestContext);
						logDokumentoversiktBrukerQueryDone(journalposter.size(), brukeridentifikator);
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
				log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med aktoerId={}", numJournalposter, brukeridentifikator.getIdent());
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
