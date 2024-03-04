package no.nav.saf.graphql;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.language.SourceLocation;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Override
	public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
		Throwable exception = handlerParameters.getException();
		String path = handlerParameters.getPath().segmentToString();
		SourceLocation sourceLocation = handlerParameters.getSourceLocation();

		return CompletableFuture.completedFuture(
				DataFetcherExceptionHandlerResult.newResult()
						.error(categorizeThrowableLogAndCreateError(exception, path, sourceLocation))
						.build()
		);
	}

	public static GraphQLError categorizeThrowableLogAndCreateError(Throwable exception, String path, SourceLocation... sourceLocations) {
		if (exception instanceof SafFunctionalException e) {
			log.warn("query {} funksjonell feil. melding={}", path, e.getMessage(), e);
			for (SourceLocation location : sourceLocations) {
				e.addLocation(location);
			}
			return e;
		} else if (exception instanceof SafTechnicalException e) {
			log.error("query {} teknisk feil. melding={}", path, e.getMessage(), e);
			return e.asAnonymizedGraphQlError();
		} else {
			log.error("query {} ukjent teknisk feil. melding={}", path, exception.getMessage(), exception);
			return new SafTechnicalException("Ukjent teknisk feil. Meld fra til #team_dokumentløsninger på Slack.", exception)
					.asAnonymizedGraphQlError();
		}
	}
}
