package no.nav.saf.query.journalpost;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static no.nav.saf.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.saf.util.MDCConstants.EKSTERNREFERANSE_ID;
import static no.nav.saf.util.MDCConstants.JOURNALPOST_ID;
import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Slf4j
@Component
public class JournalpostDataFetcher implements DataFetcher<DataFetcherResult<Journalpost>> {

	private final JournalpostQuery journalpostQuery;

	public JournalpostDataFetcher(JournalpostQuery journalpostQuery) {
		this.journalpostQuery = journalpostQuery;
	}

	@Override
	public DataFetcherResult<Journalpost> get(DataFetchingEnvironment environment) throws Exception {
		SafRequestContext safRequestContext = environment.getGraphQlContext().get(SafRequestContext.KEY);
		addMdcData(safRequestContext);
		final String journalpostId = environment.getArgument("journalpostId");
		final String eksternReferanseId = environment.getArgument("eksternReferanseId");
		mdcSporing(journalpostId, eksternReferanseId);
		validateJournalpostIdOgEksternReferanseId(journalpostId, eksternReferanseId);

		String argumentNameValue = argumentNameValue(journalpostId, eksternReferanseId);
		log.info("query journalpost({})", argumentNameValue);
		Journalpost journalpost = journalpostQuery.hentJournalpost(journalpostId, eksternReferanseId, safRequestContext, environment);
		log.info("query journalpost({}) hentet journalpost", argumentNameValue);

		return DataFetcherResult.<Journalpost>newResult()
				.data(journalpost)
				.build();
	}

	private String argumentNameValue(String journalpostId, String eksternReferanseId) {
		if (isBlank(journalpostId) && isNotBlank(eksternReferanseId)) {
			return "eksternReferanseId=" + eksternReferanseId;
		} else {
			return "journalpostId=" + journalpostId;
		}
	}

	private static void mdcSporing(String journalpostId, String eksternReferanseId) {
		if (isNotBlank(journalpostId)) {
			MDC.put(JOURNALPOST_ID, journalpostId);
		}
		if (isNotBlank(journalpostId)) {
			MDC.put(EKSTERNREFERANSE_ID, eksternReferanseId);
		}
	}

	private void validateJournalpostIdOgEksternReferanseId(String journalpostId, String eksternReferanseId) {
		if (isNotBlank(journalpostId) && !isNumeric(journalpostId)) {
			throw new SafFunctionalException(BAD_REQUEST, "journalpostId er en ikke-numerisk verdi.");
		}

		if (isBlank(journalpostId)) {
			if (isNotBlank(eksternReferanseId) && eksternReferanseId.length() > 200) {
				throw new SafFunctionalException(BAD_REQUEST, "eksternReferanseId kan ha maks 200 tegn.");
			}
		}

		if (isBlank(journalpostId) && isBlank(eksternReferanseId)) {
			throw new SafFunctionalException(BAD_REQUEST, "journalpostId og eksternReferanseId kan ikke være tomt eller null.");
		}
	}
}

