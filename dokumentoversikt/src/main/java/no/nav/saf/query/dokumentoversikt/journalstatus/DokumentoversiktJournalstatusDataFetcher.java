package no.nav.saf.query.dokumentoversikt.journalstatus;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import static no.nav.saf.util.MDCUtility.addMdcData;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentoversiktJournalstatusDataFetcher implements DataFetcher<DataFetcherResult<Dokumentoversikt>> {

	private final DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery;

	public DokumentoversiktJournalstatusDataFetcher(DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery) {
		this.dokumentoversiktJournalstatusQuery = dokumentoversiktJournalstatusQuery;
	}

	@Override
	public DataFetcherResult<Dokumentoversikt> get(DataFetchingEnvironment environment) throws Exception {
		try {
			SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
			addMdcData(safRequestContext);
			DokumentoversiktJournalstatusArguments arguments = DokumentoversiktJournalstatusArguments.create(environment);
			log.info("dokumentoversiktJournalstatus hentes for filter={}", arguments.getFilters());
			Dokumentoversikt dokumentoversikt = dokumentoversiktJournalstatusQuery.hentDokumentoversikt(arguments, safRequestContext);
			log.info("dokumentoversiktJournalstatus returnerer {} journalposter for filter={}",
					dokumentoversikt.getJournalposter().size(), arguments.getFilters());
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(dokumentoversikt)
					.build();
		} catch (SafFunctionalException e) {
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(Dokumentoversikt.empty())
					.error(e)
					.build();
		}

	}
}
