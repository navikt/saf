package no.nav.saf.query.dokumentoversikt.journalstatus;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.graphql.GraphQLExceptionHandler;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
public class DokumentoversiktJournalstatusDataFetcher implements DataFetcher<DataFetcherResult<Dokumentoversikt>> {

	private final DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery;

	public DokumentoversiktJournalstatusDataFetcher(DokumentoversiktJournalstatusQuery dokumentoversiktJournalstatusQuery) {
		this.dokumentoversiktJournalstatusQuery = dokumentoversiktJournalstatusQuery;
	}

	@Override
	public DataFetcherResult<Dokumentoversikt> get(DataFetchingEnvironment environment) throws Exception {
		SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
		addMdcData(safRequestContext);
		try {
			var unsupportedFields = environment.getSelectionSet().getFields("**/brukerHarTilgang", "**/brukerTilgangAvvistBegrunnelser");
			if (!unsupportedFields.isEmpty()) {
				final String errorMessage = createErrorMessageAndLog(unsupportedFields);
				return createDataFetcherErrorResponse(new SafFunctionalException(errorMessage));
			}
			DokumentoversiktJournalstatusArguments arguments = DokumentoversiktJournalstatusArguments.create(environment);
			log.info("dokumentoversiktJournalstatus hentes for filter={}", arguments.getFilters());
			Dokumentoversikt dokumentoversikt = dokumentoversiktJournalstatusQuery.hentDokumentoversikt(arguments, safRequestContext);
			log.info("dokumentoversiktJournalstatus returnerer {} journalposter for filter={}",
					dokumentoversikt.getJournalposter().size(), arguments.getFilters());
			return DataFetcherResult.<Dokumentoversikt>newResult()
					.data(dokumentoversikt)
					.build();
		} catch (Exception e) {
			return createDataFetcherErrorResponse(GraphQLExceptionHandler.categorizeThrowableLogAndCreateError(e, "DokumentoversiktJournalstatus"));
		}
	}

	private static DataFetcherResult<Dokumentoversikt> createDataFetcherErrorResponse(GraphQLError graphQLError) {
		return DataFetcherResult.<Dokumentoversikt>newResult()
				.data(Dokumentoversikt.empty())
				.error(graphQLError)
				.build();
	}

	private static String createErrorMessageAndLog(List<SelectedField> unsupportedFields) {
		String feilmelding = prettyPrintList(unsupportedFields.stream().map(SelectedField::getQualifiedName).toArray(String[]::new));
		String fieldPluralSingular = unsupportedFields.size() == 1 ? "Feltet" : "Feltene";

		log.warn("query DokumentoversiktJournalstatus funksjonell feil. {} {} er med i queriet, men de{} er ikke støttet her.",
				fieldPluralSingular, feilmelding, unsupportedFields.size() == 1 ? "t" : "");

		return fieldPluralSingular + " " + feilmelding + " er ikke støttet i DokumentoversiktJournalstatus-queriet";
	}

	private static String prettyPrintList(String... list) {
		if (list.length == 1) {
			return list[0];
		}

		String commaSeparated = "";
		int length = list.length - 1;
		for (int i = 0; i < length - 1; i++) {
			commaSeparated += list[i] + ", ";
		}
		return commaSeparated + list[length - 1] + " og " + list[length];
	}
}
