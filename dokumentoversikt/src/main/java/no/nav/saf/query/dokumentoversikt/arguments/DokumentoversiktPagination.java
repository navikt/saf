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

	@Value
	public static class SeekPagination {
		private final Integer foerste;
		private final String etterPeker;
	}

	public static SeekPagination create(DataFetchingEnvironment environment) {
		if (environment.getArgument("foerste") == null) {
			throw new IllegalArgumentException("Du må angi en `foerste` verdi for å paginere.");
		}
		Integer foerste = environment.getArgument("foerste");
		if(foerste <= 0) {
			throw new IllegalArgumentException("`foerste` kan ikke være 0 eller negativ.");
		}
		String etterPeker = environment.getArgument("etter");
		return new SeekPagination(foerste, etterPeker);
	}
}
