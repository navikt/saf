package no.nav.saf.query.dokumentoversikt.fagsak;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tjeneste.argumenter.FagsakInput;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktFagsakArguments extends AbstractDokumentoversiktArguments {
	static final String INFOTRYGD_FAGSAKSYSTEM = "IT01";
	FagsakInput fagsakInput;

	private DokumentoversiktFagsakArguments(FagsakInput fagsakInput,
											DokumentoversiktFilters filters,
											DokumentoversiktPagination.SeekPagination pagination) {
		super(filters, pagination);
		validate(fagsakInput);
		this.fagsakInput = fagsakInput;
	}

	private void validate(FagsakInput fagsakInput) {
		if (INFOTRYGD_FAGSAKSYSTEM.equals(fagsakInput.getFagsaksystem())) {
			throw new UnsupportedFagsakSystemException("fagsakId.fagsaksystem IT01 (Infotrygd) støttes ikke i query dokumentoversiktFagsak. " +
					"Dette er fordi en fagsak ikke er unik for en bruker i Infotrygd.");
		}
	}

	public static DokumentoversiktFagsakArguments create(DataFetchingEnvironment environment) {
		Map<String, Object> fagsakId = environment.getArgument("fagsak");
		FagsakInput fagsakInput = new FagsakInput((String) fagsakId.get("fagsakId"), (String) fagsakId.get("fagsaksystem"));
		return new DokumentoversiktFagsakArguments(fagsakInput, DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}
}
