package no.nav.saf.endpoints.wiring;

import graphql.execution.DataFetcherResult;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktCoordinator;
import no.nav.saf.query.dokumentoversikt.bruker.DokumentoversiktBrukerArguments;
import no.nav.saf.query.dokumentoversikt.bruker.DokumentoversiktBrukerCoordinator;
import no.nav.saf.query.dokumentoversikt.fagsak.DokumentoversiktFagsakArguments;
import no.nav.saf.query.dokumentoversikt.fagsak.DokumentoversiktFagsakCoordinator;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class DokumentoversiktWiring {
	private final DokumentoversiktCoordinator dokumentoversiktCoordinator;
	private final DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator;
	private final DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator;

	@Inject
	public DokumentoversiktWiring(DokumentoversiktCoordinator dokumentoversiktCoordinator,
								  DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator,
								  DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator) {
		this.dokumentoversiktCoordinator = dokumentoversiktCoordinator;
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
						DokumentoversiktBrukerArguments arguments = DokumentoversiktBrukerArguments.create(environment);
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.setCorrelationId(environment.getExecutionId());
						log.info("dokumentoversiktBruker hentes for bruker med {}", arguments.getBrukerIdInput());
						Dokumentoversikt dokumentoversikt = dokumentoversiktBrukerCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med {}", dokumentoversikt.getJournalposter()
								.size(), arguments.getBrukerIdInput());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<Dokumentoversikt>(Dokumentoversikt.empty(), Collections.singletonList(e));
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktFagsak", environment -> {
					try {
						DokumentoversiktFagsakArguments arguments = DokumentoversiktFagsakArguments.create(environment);
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.setCorrelationId(environment.getExecutionId());
						log.info("dokumentoversiktFagsak hentes for fagsakIdInput={}", arguments.getFagsakInput());
						Dokumentoversikt dokumentoversikt = dokumentoversiktFagsakCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktFagsak returnerer {} journalposter for fagsakId={}",
								dokumentoversikt.getJournalposter().size(), arguments.getFagsakInput());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return new DataFetcherResult<Dokumentoversikt>(Dokumentoversikt.empty(), Collections.singletonList(e));
					}
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					Journalpost journalpost = environment.getSource();
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktCoordinator.findDokumenter(journalpost, safRequestContext);
				}))
				.build();
	}

}
