package no.nav.saf.endpoints.wiring;

import graphql.execution.DataFetcherResult;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktCoordinator;
import no.nav.saf.query.dokumentoversikt.bruker.DokumentoversiktBrukerArguments;
import no.nav.saf.query.dokumentoversikt.bruker.DokumentoversiktBrukerCoordinator;
import no.nav.saf.query.dokumentoversikt.fagsak.DokumentoversiktFagsakArguments;
import no.nav.saf.query.dokumentoversikt.fagsak.DokumentoversiktFagsakCoordinator;
import no.nav.saf.query.dokumentoversikt.journalstatus.DokumentoversiktJournalstatusArguments;
import no.nav.saf.query.dokumentoversikt.journalstatus.DokumentoversiktJournalstatusCoordinator;
import no.nav.saf.query.journalpost.JournalpostCoordinator;
import no.nav.saf.query.tilknyttedejournalposter.TilknyttedeJournalposterCoordinator;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class DokumentoversiktWiring {
	private final DokumentoversiktCoordinator dokumentoversiktCoordinator;
	private final DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator;
	private final DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator;
	private final DokumentoversiktJournalstatusCoordinator dokumentoversiktJournalstatusCoordinator;
	private final JournalpostCoordinator journalpostCoordinator;
	private final TilknyttedeJournalposterCoordinator tilknyttedeJournalposterCoordinator;
	private final MeterRegistry meterRegistry;

	@Inject
	public DokumentoversiktWiring(DokumentoversiktCoordinator dokumentoversiktCoordinator,
								  DokumentoversiktBrukerCoordinator dokumentoversiktBrukerCoordinator,
								  DokumentoversiktFagsakCoordinator dokumentoversiktFagsakCoordinator,
								  DokumentoversiktJournalstatusCoordinator dokumentoversiktJournalstatusCoordinator,
								  JournalpostCoordinator journalpostCoordinator,
								  TilknyttedeJournalposterCoordinator tilknyttedeJournalposterCoordinator,
								  MeterRegistry meterRegistry) {
		this.dokumentoversiktCoordinator = dokumentoversiktCoordinator;
		this.dokumentoversiktBrukerCoordinator = dokumentoversiktBrukerCoordinator;
		this.dokumentoversiktFagsakCoordinator = dokumentoversiktFagsakCoordinator;
		this.dokumentoversiktJournalstatusCoordinator = dokumentoversiktJournalstatusCoordinator;
		this.journalpostCoordinator = journalpostCoordinator;
		this.tilknyttedeJournalposterCoordinator = tilknyttedeJournalposterCoordinator;
		this.meterRegistry = meterRegistry;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DateScalar.DATE)
				.scalar(DateTimeScalar.DATE_TIME)
				.type("Tema", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Tema.class)))
				.type("Journalposttype", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalposttype.class)))
				.type("Journalstatus", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Journalstatus.class)))
				.type("Tilknytning", typeWiring -> typeWiring.enumValues(new NaturalEnumValuesProvider<>(Tilknytning.class)))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktBruker", environment -> {
					try {
						DokumentoversiktBrukerArguments arguments = DokumentoversiktBrukerArguments.create(environment);
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.getSecurityContext().getOidcTokenBody();
						log.info("dokumentoversiktBruker hentes for bruker med {}", arguments.getBrukerIdInput());
						Dokumentoversikt dokumentoversikt = dokumentoversiktBrukerCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med {}", dokumentoversikt.getJournalposter()
								.size(), arguments.getBrukerIdInput());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return DataFetcherResult.newResult()
								.data(Dokumentoversikt.empty())
								.error(e)
								.build();
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktFagsak", environment -> {
					try {
						DokumentoversiktFagsakArguments arguments = DokumentoversiktFagsakArguments.create(environment);
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.getSecurityContext().getOidcTokenBody();
						log.info("dokumentoversiktFagsak hentes for fagsakIdInput={}", arguments.getFagsakInput());
						Dokumentoversikt dokumentoversikt = dokumentoversiktFagsakCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktFagsak returnerer {} journalposter for fagsakId={}",
								dokumentoversikt.getJournalposter().size(), arguments.getFagsakInput());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return DataFetcherResult.newResult()
								.data(Dokumentoversikt.empty())
								.error(e)
								.build();
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktJournalstatus", environment -> {
					try {
						DokumentoversiktJournalstatusArguments arguments = DokumentoversiktJournalstatusArguments.create(environment);
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.getSecurityContext().getOidcTokenBody();
						log.info("dokumentoversiktJournalstatus hentes for filter={}", arguments.getFilters());
						Dokumentoversikt dokumentoversikt = dokumentoversiktJournalstatusCoordinator.hentDokumentoversikt(arguments, safRequestContext);
						log.info("dokumentoversiktJournalstatus returnerer {} journalposter for filter={}",
								dokumentoversikt.getJournalposter().size(), arguments.getFilters());
						return dokumentoversikt;
					} catch (SafFunctionalException e) {
						return DataFetcherResult.newResult()
								.data(Dokumentoversikt.empty())
								.error(e)
								.build();
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("journalpost", environment -> {
					try {
						final String journalpostId = environment.getArgument("journalpostId");
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.getSecurityContext().getOidcTokenBody();
						log.info("query journalpost for journalpostId={}", journalpostId);
						Journalpost journalpost = journalpostCoordinator.hentJournalpost(journalpostId, safRequestContext);
						log.info("journalpost hentet for journalpostId={}", journalpostId);
						return journalpost;
					} catch (SafFunctionalException e) {
						return DataFetcherResult.newResult()
								.data(null)
								.error(e)
								.build();
					}
				}))
				.type("Query", typeWiring -> typeWiring.dataFetcher("tilknyttedeJournalposter", environment -> {
					try {
						final String dokumentInfoId = environment.getArgument("dokumentInfoId");
						final Tilknytning tilknytning = environment.getArgument("tilknytning");
						SafRequestContext safRequestContext = environment.getContext();
						safRequestContext.getSecurityContext().getOidcTokenBody();
						log.info("tilknyttedeJournalposter for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);
						List<Journalpost> tilknyttedeJournalposter = tilknyttedeJournalposterCoordinator.hentTilknyttedeJournalposter(dokumentInfoId, tilknytning, safRequestContext);
						log.info("tilknyttedeJournalposter hentet for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);
						return tilknyttedeJournalposter;
					} catch (SafFunctionalException e) {
						return DataFetcherResult.newResult()
								.data(new ArrayList<>())
								.error(e)
								.build();
					}
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					Journalpost journalpost = environment.getSource();
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktCoordinator.findDokumenter(journalpost, safRequestContext);
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("journalforendeEnhet", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Journalpost journalpost = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Journalpost.journalforendeEnhet")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return journalpost.getJournalforendeEnhet();
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("avsenderMottakerId", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Journalpost journalpost = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Journalpost.avsenderMottakerId")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return journalpost.getAvsenderMottakerId();
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("avsenderMottakerNavn", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Journalpost journalpost = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Journalpost.avsenderMottakerNavn")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return journalpost.getAvsenderMottakerNavn();
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("avsenderMottakerLand", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Journalpost journalpost = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Journalpost.avsenderMottakerLand")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return journalpost.getAvsenderMottakerLand();
				}))
				.type("Sak", typeWiring -> typeWiring.dataFetcher("arkivsaksnummer", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Sak sak = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Sak.arkivsaksnummer")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return sak.getArkivsaksnummer();
				}))
				.type("Sak", typeWiring -> typeWiring.dataFetcher("arkivsaksystem", environment -> {
					SafRequestContext safRequestContext = environment.getContext();
					Sak sak = environment.getSource();
					Counter.builder("dok_saf_deprecated_field")
							.tags("fieldname", "Sak.arkivsaksystem")
							.tags("consumer_id", safRequestContext.getSecurityContext().getNavConsumerId())
							.register(meterRegistry)
							.increment();
					return sak.getArkivsaksystem();
				}))
				.build();
	}
}
