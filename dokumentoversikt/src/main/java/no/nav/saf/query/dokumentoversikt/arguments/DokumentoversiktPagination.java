package no.nav.saf.query.dokumentoversikt.arguments;

import graphql.schema.DataFetchingEnvironment;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class DokumentoversiktPagination {
	private DokumentoversiktPagination() {
		// ingen instansiering
	}

	public interface Pagination {
	}

	@Value
	public static class SeekPagination implements Pagination {
		private final Integer foerste;
		private final String etterPeker;
		// det er ikke noe behov for å paginere bakover
		@Deprecated
		private final Integer siste;
		// det er ikke noe behov for å paginere bakover
		@Deprecated
		private final String foerPeker;
	}

	public static Pagination create(DataFetchingEnvironment environment) {
		if (environment.getArgument("foerste") == null) {
			throw new IllegalArgumentException("Du må angi en `foerste` verdi for å paginere.");
		}
		Integer foerste = environment.getArgument("foerste");
		if(foerste <= 0) {
			throw new IllegalArgumentException("`foerste` kan ikke være 0 eller negativ.");
		}
		String etterPeker = environment.getArgument("etter");
		return new SeekPagination(foerste, etterPeker, null, null);
	}
}
