package no.nav.saf.query.sak;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
public class SakerDataFetcher implements DataFetcher<DataFetcherResult<List<Sak>>> {

	private final SakerQuery sakerQuery;

	public SakerDataFetcher(SakerQuery sakerQuery) {
		this.sakerQuery = sakerQuery;
	}

	@Override
	public DataFetcherResult<List<Sak>> get(DataFetchingEnvironment environment) throws Exception {
		SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
		addMdcData(safRequestContext);
		try {
			Map<String, Object> brukerId = environment.getArgument("brukerId");
			final BrukerIdInput brukerIdInput = new BrukerIdInput((String) brukerId.get("id"), BrukerIdType.valueOf((String) brukerId.get("type")));
			List<Sak> tilknyttedeSaker = sakerQuery.hentSaker(brukerIdInput, safRequestContext);
			log.info("Saker hentet {} saker for bruker", tilknyttedeSaker.size());
			return DataFetcherResult.<List<Sak>>newResult()
					.data(tilknyttedeSaker)
					.build();
		} catch (SafFunctionalException e) {
			return DataFetcherResult.<List<Sak>>newResult()
					.data(new ArrayList<>())
					.error(e)
					.build();
		}
	}
}
