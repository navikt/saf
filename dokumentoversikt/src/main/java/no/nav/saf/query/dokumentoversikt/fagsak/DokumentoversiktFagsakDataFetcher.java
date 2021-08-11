package no.nav.saf.query.dokumentoversikt.fagsak;

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
public class DokumentoversiktFagsakDataFetcher implements DataFetcher<DataFetcherResult<Dokumentoversikt>> {

	private final DokumentoversiktFagsakQuery dokumentoversiktFagsakQuery;

	public DokumentoversiktFagsakDataFetcher(DokumentoversiktFagsakQuery dokumentoversiktFagsakQuery) {
		this.dokumentoversiktFagsakQuery = dokumentoversiktFagsakQuery;
	}

	@Override
	public DataFetcherResult<Dokumentoversikt> get(DataFetchingEnvironment environment) throws Exception {
		try {
			DokumentoversiktFagsakArguments arguments = DokumentoversiktFagsakArguments.create(environment);
			SafRequestContext safRequestContext = environment.getContext();
			safRequestContext.getSecurityContext().getOidcTokenBody();
			log.info("dokumentoversiktFagsak hentes for fagsakIdInput={}", arguments.getFagsakInput());
			Dokumentoversikt dokumentoversikt = dokumentoversiktFagsakQuery.hentDokumentoversikt(arguments, safRequestContext);
			log.info("dokumentoversiktFagsak returnerer {} journalposter for fagsakId={}",
					dokumentoversikt.getJournalposter().size(), arguments.getFagsakInput());
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
