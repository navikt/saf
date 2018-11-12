package no.nav.saf.endpoints.wiring;

import graphql.execution.DataFetcherResult;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.dokumentoversikt.DokumentoversiktArguments;
import no.nav.saf.tjeneste.dokumentoversikt.DokumentoversiktDomainCoordinator;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
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
public class DokumentoversiktWiring {
	private final DokumentoversiktDomainCoordinator dokumentoversiktDomainCoordinator;

	@Inject
	public DokumentoversiktWiring(DokumentoversiktDomainCoordinator dokumentoversiktDomainCoordinator) {
		this.dokumentoversiktDomainCoordinator = dokumentoversiktDomainCoordinator;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DateScalar.DATE)
				.scalar(DateTimeScalar.DATE_TIME)
				.type("JournalpostType", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(JournalpostType.class)))
				.type("JournalStatus", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(JournalStatus.class)))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktBruker", environment -> {
					String aktoerId = environment.getArgument("aktoerId");
					LocalDate fraDato = environment.getArgument("fraDato");
					List<JournalpostType> journalposttyper = environment.getArgument("journalposttyper");
					List<JournalStatus> journalstatuser = environment.getArgument("journalstatuser");
					boolean visFeilregistrerte = environment.getArgument("visFeilregistrerte");
					SafRequestContext safRequestContext = environment.getContext();
					try {
						return dokumentoversiktDomainCoordinator.findJournalposter(new DokumentoversiktArguments(aktoerId, fraDato, journalposttyper, journalstatuser, visFeilregistrerte),
								safRequestContext);
					} catch(SafFunctionalException e) {
						return new DataFetcherResult<List<Journalpost>>(new ArrayList<>(), Collections.singletonList(e));
					}
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					Journalpost journalpost = environment.getSource();
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktDomainCoordinator.findDokumenter(journalpost, safRequestContext);
				}))
				.build();
	}
}
