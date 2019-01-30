package no.nav.saf.query.dokumentoversikt.fagsak;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tjeneste.argumenter.FagsakInput;

import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktFagsakArguments extends AbstractDokumentoversiktArguments {
	private final FagsakInput fagsakInput;

	public DokumentoversiktFagsakArguments(FagsakInput fagsakInput,
										   DokumentoversiktFilters filters,
										   DokumentoversiktPagination.Pagination pagination) {
		super(filters, pagination);
		this.fagsakInput = fagsakInput;
	}

	public static DokumentoversiktFagsakArguments create(DataFetchingEnvironment environment) {
		Map<String, Object> fagsakId = environment.getArgument("fagsak");
		FagsakInput fagsakInput = new FagsakInput((String) fagsakId.get("fagsakId"), (String) fagsakId.get("fagsaksystem"));
		return new DokumentoversiktFagsakArguments(fagsakInput, DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}
}
