package no.nav.saf.query.dokumentoversikt.journalstatus;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktJournalstatusArguments extends AbstractDokumentoversiktArguments {

	public DokumentoversiktJournalstatusArguments(DokumentoversiktFilters filters,
												  DokumentoversiktPagination.SeekPagination pagination) {
		super(filters, pagination);
	}

	public static DokumentoversiktJournalstatusArguments create(DataFetchingEnvironment environment) {
		return new DokumentoversiktJournalstatusArguments(DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}
}
