package no.nav.saf.query.dokumentoversikt.fagsak;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tjeneste.argumenter.FagsakIdInput;

import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktFagsakArguments extends AbstractDokumentoversiktArguments {
	private final FagsakIdInput fagsakIdInput;

	public DokumentoversiktFagsakArguments(FagsakIdInput fagsakIdInput,
										   DokumentoversiktFilters filters,
										   DokumentoversiktPagination.Pagination pagination) {
		super(filters, pagination);
		this.fagsakIdInput = fagsakIdInput;
	}

	public static DokumentoversiktFagsakArguments create(DataFetchingEnvironment environment) {
		Map<String, Object> fagsakId = environment.getArgument("fagsakId");
		FagsakIdInput fagsakIdInput = new FagsakIdInput((String) fagsakId.get("fagsaksnummer"), (String) fagsakId.get("fagsaksystem"));
		return new DokumentoversiktFagsakArguments(fagsakIdInput, DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}
}
