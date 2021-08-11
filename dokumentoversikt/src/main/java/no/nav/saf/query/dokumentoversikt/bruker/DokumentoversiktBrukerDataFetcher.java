package no.nav.saf.query.dokumentoversikt.bruker;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
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
			DokumentoversiktBrukerArguments arguments = DokumentoversiktBrukerArguments.create(environment);
			SafRequestContext safRequestContext = environment.getContext();
			safRequestContext.getSecurityContext().getOidcTokenBody();
			log.info("dokumentoversiktBruker hentes for bruker med {}", arguments.getBrukerIdInput());
			Dokumentoversikt dokumentoversikt = dokumentoversiktBrukerQuery.hentDokumentoversikt(arguments, safRequestContext);
			log.info("dokumentoversiktBruker returnerer {} journalposter for bruker med {}", dokumentoversikt.getJournalposter()
					.size(), arguments.getBrukerIdInput());
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
