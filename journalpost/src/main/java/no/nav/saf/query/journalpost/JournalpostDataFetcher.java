package no.nav.saf.query.journalpost;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class JournalpostDataFetcher implements DataFetcher<DataFetcherResult<Journalpost>> {

	private final JournalpostQuery journalpostQuery;

	public JournalpostDataFetcher(JournalpostQuery journalpostQuery) {
		this.journalpostQuery = journalpostQuery;
	}

	@Override
	public DataFetcherResult<Journalpost> get(DataFetchingEnvironment environment) throws Exception {
		final String journalpostId = environment.getArgument("journalpostId");
		try {
			SafRequestContext safRequestContext = environment.getContext();
			safRequestContext.getSecurityContext().getOidcTokenBody();
			log.info("query journalpost for journalpostId={}", journalpostId);
			Journalpost journalpost = journalpostQuery.hentJournalpost(journalpostId, safRequestContext);
			log.info("journalpost hentet for journalpostId={}", journalpostId);
			return DataFetcherResult.<Journalpost>newResult()
					.data(journalpost)
					.build();
		} catch (JournalpostIkkeFunnetException e) {
			log.warn("Fant ikke journalpost i fagarkivet. journalpostId={}", journalpostId, e);
			return DataFetcherResult.<Journalpost>newResult()
					.data(null)
					.error(e)
					.build();
		} catch (SafFunctionalException e) {
			log.warn("Kunne ikke hente journalpost. journalpostId={}", journalpostId, e);
			return DataFetcherResult.<Journalpost>newResult()
					.data(null)
					.error(e)
					.build();
		}
	}
}
