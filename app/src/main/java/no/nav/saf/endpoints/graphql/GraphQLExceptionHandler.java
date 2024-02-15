package no.nav.saf.endpoints.graphql;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;

@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Override
	public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
		Throwable exception = handlerParameters.getException();
		SourceLocation sourceLocation = handlerParameters.getSourceLocation();
		ResultPath path = handlerParameters.getPath();
		DataFetchingEnvironment environment = handlerParameters.getDataFetchingEnvironment();

		DataFetcherExceptionHandlerResult result;
		if (exception instanceof SafFunctionalException e) {
			log.warn("query {} feilet funksjonelt. melding={}", path.segmentToString(), e.getMessage(), e);
			result = DataFetcherExceptionHandlerResult.newResult()
					.error(e)
					.build();
		} else if (exception instanceof SafTechnicalException e) {
			log.error("query {} teknisk feil. melding={}", path.segmentToString(), e.getMessage(), e);
			result = DataFetcherExceptionHandlerResult.newResult()
					.error(e.asAnonymizedGraphQlError())
					.build();
		} else if (exception instanceof Exception e) {
			log.error("query {} ukjent teknisk feil. melding={}", path.segmentToString(), e.getMessage(), e);
			result = DataFetcherExceptionHandlerResult.newResult()
					.error(SERVER_ERROR.construct(environment,
							"Ukjent teknisk feil. Meld fra til #team_dokumentløsninger på Slack."))
					.build();
		} else {
			CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
			log.error("Kall til graphql feilet teknisk. path={}, errormsg={}", path, exception.getMessage(), exception);
			result = DataFetcherExceptionHandlerResult.newResult()
					.error(error)
					.build();
		}
		return CompletableFuture.completedFuture(result);
	}
}
