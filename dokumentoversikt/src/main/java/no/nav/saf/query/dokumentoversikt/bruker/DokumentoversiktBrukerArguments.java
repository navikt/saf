package no.nav.saf.query.dokumentoversikt.bruker;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;

import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktBrukerArguments extends AbstractDokumentoversiktArguments {
	private final BrukerIdInput brukerIdInput;

	public DokumentoversiktBrukerArguments(BrukerIdInput brukerIdInput,
										   DokumentoversiktFilters filters,
										   DokumentoversiktPagination.Pagination pagination) {
		super(filters, pagination);
		this.brukerIdInput = brukerIdInput;
	}

	public static DokumentoversiktBrukerArguments create(DataFetchingEnvironment environment) {
		Map<String, Object> brukerId = environment.getArgument("brukerId");
		BrukerIdInput brukerIdInput = new BrukerIdInput((String) brukerId.get("id"), BrukerIdType.valueOf((String) brukerId.get("type")));
		return new DokumentoversiktBrukerArguments(brukerIdInput, DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}
}

