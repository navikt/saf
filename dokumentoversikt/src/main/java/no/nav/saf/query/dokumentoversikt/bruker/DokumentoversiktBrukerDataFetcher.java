package no.nav.saf.query.dokumentoversikt.bruker;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
public class DokumentoversiktBrukerDataFetcher implements DataFetcher<DataFetcherResult<Dokumentoversikt>> {

	private final DokumentoversiktBrukerQuery dokumentoversiktBrukerQuery;

	public DokumentoversiktBrukerDataFetcher(DokumentoversiktBrukerQuery dokumentoversiktBrukerQuery) {
		this.dokumentoversiktBrukerQuery = dokumentoversiktBrukerQuery;
	}

	@Override
	public DataFetcherResult<Dokumentoversikt> get(DataFetchingEnvironment environment) throws Exception {
		try {
			SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
			addMdcData(safRequestContext);
			DokumentoversiktBrukerArguments arguments = DokumentoversiktBrukerArguments.create(environment);
			log.info("query dokumentoversiktBruker hentes for bruker med {}", arguments.getBrukerIdInput());
			Dokumentoversikt dokumentoversikt = dokumentoversiktBrukerQuery.hentDokumentoversikt(arguments, safRequestContext, environment);
			log.info("query dokumentoversiktBruker returnerer {} journalposter for bruker med {}", dokumentoversikt.getJournalposter()
					.size(), arguments.getBrukerIdInput());
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(dokumentoversikt)
					.build();
		// } catch (GraphQLException e) {
		// 	log.warn("query dokumentoversiktBruker feilet. melding={}", e.getError().getMessage(), e);
		// 	return e.toDataFetcherResult();
		} catch (SafFunctionalException e) { // flytt dette til vår venn
			log.warn("query dokumentoversiktBruker feilet funksjonelt. melding={}", e.getMessage(), e);
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(Dokumentoversikt.empty())
					.error(e)
					.build();
		} catch (SafTechnicalException e) {
			log.error("query dokumentoversiktBruker teknisk feil. melding={}", e.getMessage(), e);
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.error(SERVER_ERROR.construct(environment,
							"Teknisk feil. Prøv igjen senere."))
					.build();
		} catch (Exception e) {
			log.error("query dokumentoversiktBruker ukjent teknisk feil. melding={}", e.getMessage(), e);
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.error(SERVER_ERROR.construct(environment,
							"Ukjent teknisk feil. Meld fra til #team_dokumentløsninger på Slack."))
					.build();
		}
	}
}
