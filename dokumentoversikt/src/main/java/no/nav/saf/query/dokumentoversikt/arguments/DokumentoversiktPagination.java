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
		private final Integer siste;
		private final String foerPeker;
	}

	public static Pagination create(DataFetchingEnvironment environment) {
		if (environment.getArgument("foerste") != null && environment.getArgument("siste") != null) {
			throw new IllegalArgumentException("Det er ikke tillatt å angi både `foerste` og `siste` for å paginere.");
		}
		if (environment.getArgument("foerste") == null && environment.getArgument("siste") == null) {
			throw new IllegalArgumentException("Du må angi en `foerste` eller en `siste` verdi for å paginere.");
		}
		Integer foerste = environment.getArgument("foerste");
		String etterPeker = environment.getArgument("etter");
		Integer siste = environment.getArgument("siste");
		String foerPeker = environment.getArgument("foer");
		return new SeekPagination(foerste, etterPeker, siste, foerPeker);
	}
}
