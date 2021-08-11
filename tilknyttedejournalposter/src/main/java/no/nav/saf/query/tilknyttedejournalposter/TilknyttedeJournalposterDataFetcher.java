package no.nav.saf.query.tilknyttedejournalposter;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class TilknyttedeJournalposterDataFetcher implements DataFetcher<DataFetcherResult<List<Journalpost>>> {

	private final TilknyttedeJournalposterQuery tilknyttedeJournalposterQuery;

	public TilknyttedeJournalposterDataFetcher(TilknyttedeJournalposterQuery tilknyttedeJournalposterQuery) {
		this.tilknyttedeJournalposterQuery = tilknyttedeJournalposterQuery;
	}

	@Override
	public DataFetcherResult<List<Journalpost>> get(DataFetchingEnvironment environment) throws Exception {
		try {
			final String dokumentInfoId = environment.getArgument("dokumentInfoId");
			final Tilknytning tilknytning = environment.getArgument("tilknytning");
			SafRequestContext safRequestContext = environment.getContext();
			safRequestContext.getSecurityContext().getOidcTokenBody();
			log.info("tilknyttedeJournalposter for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);
			List<Journalpost> tilknyttedeJournalposter = tilknyttedeJournalposterQuery.hentTilknyttedeJournalposter(dokumentInfoId, tilknytning, safRequestContext);
			log.info("tilknyttedeJournalposter hentet for dokumentInfoId={}, tilknytning={}", dokumentInfoId, tilknytning);
			return DataFetcherResult.<List<Journalpost>>newResult()
					.data(tilknyttedeJournalposter)
					.build();
		} catch (SafFunctionalException e) {
			return DataFetcherResult.<List<Journalpost>>newResult()
					.data(new ArrayList<>())
					.error(e)
					.build();
		}
	}
}
