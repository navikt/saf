package no.nav.saf.query.dokumentoversikt.arguments;

import lombok.Data;

@Data
public abstract class AbstractDokumentoversiktArguments {
	private final DokumentoversiktFilters filters;
	private final DokumentoversiktPagination.SeekPagination pagination;

	protected AbstractDokumentoversiktArguments(DokumentoversiktFilters filters, DokumentoversiktPagination.SeekPagination pagination) {
		this.filters = filters;
		this.pagination = pagination;
	}
}
