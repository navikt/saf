package no.nav.saf.query.tilknyttedejournalposter;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.saf.tilgangskontroll.SafRequestContext.KEY;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
public class TilknyttedeJournalposterDataFetcher implements DataFetcher<DataFetcherResult<List<Journalpost>>> {

	private final TilknyttedeJournalposterQuery tilknyttedeJournalposterQuery;

	public TilknyttedeJournalposterDataFetcher(TilknyttedeJournalposterQuery tilknyttedeJournalposterQuery) {
		this.tilknyttedeJournalposterQuery = tilknyttedeJournalposterQuery;
	}

	@Override
	public DataFetcherResult<List<Journalpost>> get(DataFetchingEnvironment environment) throws Exception {
		SafRequestContext safRequestContext = environment.getGraphQlContext().get(KEY);
		addMdcData(safRequestContext);

		final String dokumentInfoId = environment.getArgument("dokumentInfoId");
		final Tilknytning tilknytning = environment.getArgument("tilknytning");
		log.info("tilknyttedeJournalposter for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);

		List<Journalpost> tilknyttedeJournalposter = tilknyttedeJournalposterQuery.hentTilknyttedeJournalposter(dokumentInfoId, safRequestContext);
		log.info("tilknyttedeJournalposter hentet for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);

		return DataFetcherResult.<List<Journalpost>>newResult()
				.data(tilknyttedeJournalposter)
				.build();
	}
}
