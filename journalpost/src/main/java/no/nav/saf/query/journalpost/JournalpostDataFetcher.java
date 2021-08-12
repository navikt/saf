package no.nav.saf.query.journalpost;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.graphql.GraphQLException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;

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
			log.info("query journalpost. journalpostId={}", journalpostId);
			Journalpost journalpost = journalpostQuery.hentJournalpost(journalpostId, safRequestContext, environment);
			log.info("journalpost hentet. journalpostId={}", journalpostId);
			return DataFetcherResult.<Journalpost>newResult()
					.data(journalpost)
					.build();
		} catch (GraphQLException e) {
			log.warn("query journalpost feilet: " + e.getError().getMessage());
			return e.toDataFetcherResult();
		} catch (SafTechnicalException e) {
			log.error("query journalpost teknisk feil: " + e.getMessage(), e);
			return DataFetcherResult.<Journalpost>newResult()
					.error(SERVER_ERROR.construct(environment,
							"Teknisk feil. Prøv igjen senere."))
					.build();
		} catch (Exception e) {
			log.error("query journalpost ukjent teknisk feil:" + e.getMessage(), e);
			return DataFetcherResult.<Journalpost>newResult()
					.error(SERVER_ERROR.construct(environment,
							"Ukjent teknisk feil. Meld fra til #team_dokumentløsninger på Slack."))
					.build();
		}
	}
}
