package no.nav.saf.query.dokumentoversikt.bruker;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.query.dokumentoversikt.arguments.AbstractDokumentoversiktArguments;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktFilters;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;

import java.util.Map;

import static no.nav.saf.domain.visningsmodell.BrukerIdType.AKTOERID;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.FNR;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.ORGNR;
import static no.nav.saf.graphql.ErrorCode.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Value
@EqualsAndHashCode(callSuper = true)
public class DokumentoversiktBrukerArguments extends AbstractDokumentoversiktArguments {
	BrukerIdInput brukerIdInput;

	private DokumentoversiktBrukerArguments(BrukerIdInput brukerIdInput,
											DokumentoversiktFilters filters,
											DokumentoversiktPagination.SeekPagination pagination) {
		super(filters, pagination);
		this.brukerIdInput = brukerIdInput;
	}

	public static DokumentoversiktBrukerArguments create(DataFetchingEnvironment environment) {
		Map<String, Object> brukerId = environment.getArgument("brukerId");
		BrukerIdInput brukerIdInput = new BrukerIdInput((String) brukerId.get("id"), BrukerIdType.valueOf((String) brukerId.get("type")));
		validate(brukerIdInput);
		return new DokumentoversiktBrukerArguments(brukerIdInput, DokumentoversiktFilters.create(environment), DokumentoversiktPagination.create(environment));
	}

	private static void validate(BrukerIdInput brukerIdInput) {
		switch (brukerIdInput.getType()) {
			case AKTOERID -> {
				if (!isNumeric(brukerIdInput.getId())) {
					throw new SafFunctionalException(BAD_REQUEST, "input brukerId.id må være numerisk for brukerId.idType=" + AKTOERID);
				}
			}
			case FNR -> {
				if (!isNumeric(brukerIdInput.getId())) {
					throw new SafFunctionalException(BAD_REQUEST, "input brukerId.id må være numerisk for brukerId.idType=" + FNR);
				}
			}
			case ORGNR -> {
				if (!isNumeric(brukerIdInput.getId())) {
					throw new SafFunctionalException(BAD_REQUEST, "input brukerId.id må være numerisk for brukerId.idType=" + ORGNR);
				}
			}
		}
	}
}

