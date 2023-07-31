package no.nav.saf.endpoints.graphql;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Override
	public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
		Throwable exception = handlerParameters.getException();
		SourceLocation sourceLocation = handlerParameters.getSourceLocation();
		ResultPath path = handlerParameters.getPath();

		CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
		log.error("Kall til graphql feilet teknisk. path={}, errormsg={}", path, exception.getMessage(), exception);

		DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
				.error(error)
				.build();
		return CompletableFuture.completedFuture(result);
	}
}
