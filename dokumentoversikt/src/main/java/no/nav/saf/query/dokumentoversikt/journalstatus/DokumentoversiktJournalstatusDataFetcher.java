package no.nav.saf.query.dokumentoversikt.journalstatus;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.graphql.GraphQLExceptionHandler;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
public class DokumentoversiktJournalstatusDataFetcher implements DataFetcher<DataFetcherResult<Dokumentoversikt>> {

	private final DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery;

	public DokumentoversiktJournalstatusDataFetcher(DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery) {
		this.dokumentoversiktJournalstatusQuery = dokumentoversiktJournalstatusQuery;
	}

	@Override
	public DataFetcherResult<Dokumentoversikt> get(DataFetchingEnvironment environment) throws Exception {
		SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
		addMdcData(safRequestContext);
		try {
			DokumentoversiktJournalstatusArguments arguments = DokumentoversiktJournalstatusArguments.create(environment);
			log.info("dokumentoversiktJournalstatus hentes for filter={}", arguments.getFilters());
			Dokumentoversikt dokumentoversikt = dokumentoversiktJournalstatusQuery.hentDokumentoversikt(arguments, safRequestContext);
			log.info("dokumentoversiktJournalstatus returnerer {} journalposter for filter={}",
					dokumentoversikt.getJournalposter().size(), arguments.getFilters());
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(dokumentoversikt)
					.build();
		} catch (Exception e) {
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(Dokumentoversikt.empty())
					.error(GraphQLExceptionHandler.categorizeThrowableLogAndCreateError(e, "DokumentoversiktJournalsak"))
					.build();
		}
	}
}
