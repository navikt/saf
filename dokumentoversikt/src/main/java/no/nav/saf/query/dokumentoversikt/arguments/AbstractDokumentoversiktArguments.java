package no.nav.saf.query.dokumentoversikt.arguments;

import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public abstract class AbstractDokumentoversiktArguments {
	private final DokumentoversiktFilters filters;
	private final DokumentoversiktPagination.Pagination pagination;

	protected AbstractDokumentoversiktArguments(DokumentoversiktFilters filters, DokumentoversiktPagination.Pagination pagination) {
		this.filters = filters;
		this.pagination = pagination;
	}
}
